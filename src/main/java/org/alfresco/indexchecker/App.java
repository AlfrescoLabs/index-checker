package org.alfresco.indexchecker;

import java.util.Map;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.MappingTerms;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.FacetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Command line program that gets indexed documents from SOLR 
 * and existing nodes in Alfresco DB to apply some validations
 * on the number of existing elements in both systems.
 *
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
    
    @Override
    public void run(String... args) throws Exception
    {

        // Number of documents indexed by SOLR on a core by TYPE property
        FacetResponse solrTypesCount = solrWebClient.getDocumentCountByType(SolrWebClient.ALFRESCO_CORE_NAME);
        LOG.info("Count SOLR document = {}", solrTypesCount.response.numFound);
        
        // Number of nodes in Alfresco DB on a Store excluding those marked as non indexable 
        Integer alfrescoStoreId = dbClient.getStoreId(DbClient.ALFRESCO_STORE_PROTOCOL, DbClient.ALFRESCO_STORE_IDENTIFIER);
        LOG.info("Count DB nodes = {}", dbClient.getNodeCount(alfrescoStoreId));

        // Compare the number of documents in SOLR and nodes in Alfresco DB by TYPE
        Map<String, Integer> terms = solrTypesCount.facet_counts.facet_fields.type.stream().collect(MappingTerms.collector());
        terms.forEach((k, v) -> {
            
            String localName = k.substring(k.indexOf("}") + 1, k.length());
            String uri = k.substring(k.indexOf("{") + 1, k.indexOf("}"));
            Integer dbCount = dbClient.getCountByType(alfrescoStoreId, uri, localName);
            
            if (v > dbCount)
            {
                LOG.error("SOLR indexed " + (v - dbCount) + " nodes more than the existing in database for " + k);
            }
            if (v < dbCount)
            {
                LOG.error("The database contains " + (dbCount - v) + " nodes more than SOLR Index for " + k);
            }
            
        });

    }
    
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

}
