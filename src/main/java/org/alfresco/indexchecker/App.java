package org.alfresco.indexchecker;

import java.util.Map;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.MappingTerms;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.FacetResponse;
import org.alfresco.indexchecker.solr.bean.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Command line program that gets indexed documents from SOLR 
 * and existing nodes in Alfresco DB to apply some validations
 * on the number of existing elements in both systems.
 */
@ComponentScan
@SpringBootApplication
public class App implements CommandLineRunner
{

    static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Autowired
    DbClient dbClient;
    
    @Autowired
    SolrWebClient solrWebClient;
    
    @Autowired
    NodesComparator nodesComparator;
    
    @Autowired
    PermissionsComparator permissionsComparator;
    
    @Value("${report.detailed}")
    Boolean detailedReport;
    
    @Override
    public void run(String... args) throws Exception
    {
        
        // Number of documents indexed by SOLR on a core by TYPE property
        FacetResponse solrTypesCount = solrWebClient.getDocumentCountByType(SolrWebClient.ALFRESCO_CORE_NAME);
        LOG.info("Count SOLR documents = {}", solrTypesCount.response.numFound);
        
        // Number of nodes in Alfresco DB on a Store excluding those marked as non indexable 
        Integer alfrescoStoreId = dbClient.getStoreId(DbClient.ALFRESCO_STORE_PROTOCOL, DbClient.ALFRESCO_STORE_IDENTIFIER);
        LOG.info("Count DB nodes = {}", dbClient.getNodeCount(alfrescoStoreId));

        // Compare the number of documents in SOLR and nodes in Alfresco DB by TYPE
        Map<String, Integer> terms = solrTypesCount.facet_counts.facet_fields.type.stream()
                .collect(MappingTerms.collector());
        terms.forEach((k, v) -> {

            String localName = k.substring(k.indexOf("}") + 1, k.length());
            String uri = k.substring(k.indexOf("{") + 1, k.indexOf("}"));
            Integer dbCount = dbClient.getCountByType(alfrescoStoreId, uri, localName);

            LOG.debug("Type {} has {} documents in SOLR and {} nodes in Alfresco DB", k, v, dbCount);

            if (v.intValue() != dbCount.intValue())
            {
                if (v > dbCount)
                {
                    LOG.error("SOLR indexed {} nodes more than the existing in database for {}", (v - dbCount), k);
                }
                if (v < dbCount)
                {
                    LOG.error("The database contains {} nodes more than SOLR Index for {}", (dbCount - v), k);
                }
                if (detailedReport)
                {
                    nodesComparator.logDetailedReport(alfrescoStoreId, k, uri, localName);
                }
            }

        });
        
        // Number of permissions indexed by SOLR
        SearchResponse solrAclCount = solrWebClient.getAclCount(SolrWebClient.ALFRESCO_CORE_NAME);
        LOG.info("Count SOLR permissions = {}", solrAclCount.response.numFound);
        
        // Number of permissions in Alfresco DB
        Integer dbAclCount = dbClient.getAclCount();
        LOG.info("Count DB permissions = {}", dbAclCount);
        
        if (dbAclCount.intValue() != solrAclCount.response.numFound)
        {
            if (dbAclCount.intValue() > solrAclCount.response.numFound)
            {
                LOG.error("The database contains {} permissions more than the indexed in SOLR", (dbAclCount - solrAclCount.response.numFound));
            }
            if (solrAclCount.response.numFound > dbAclCount.intValue())
            {
                LOG.error("SOLR indexed {} permissions more than the existing in the database", (solrAclCount.response.numFound - dbAclCount));
            }
            if (detailedReport)
            {
                permissionsComparator.logDetailedReport();    
            }
        }

    }
    
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

}
