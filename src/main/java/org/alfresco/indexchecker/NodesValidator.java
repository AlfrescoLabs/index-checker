package org.alfresco.indexchecker;

import java.util.Map;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.MappingTerms;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.response.FacetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Compares the number of nodes in Alfresco DB and SOLR.
 * Compares the number of nodes in Alfresco DB and SOLR by Document TYPE.
 * Logs missing nodes in Alfresco DB or SOLR if "report.detailed" properties has been set to true.
 */
@Service
public class NodesValidator
{
    
    static final Logger LOG = LoggerFactory.getLogger(NodesValidator.class);
    
    @Autowired
    DbClient dbClient;
    
    @Autowired
    SolrWebClient solrWebClient;
    
    @Autowired
    NodesCountComparator nodesCountComparator;
    
    /**
     * Logs the number of nodes in Alfresco DB and SOLR.
     * Logs the difference in the number of nodes by Document TYPE.
     * @param detailed, logs the missing nodes in Alfresco DB or SOLR.
     * @param fix, apply fix actions to SOLR Index
     */
    public void validate(boolean detailed, boolean fix) throws JsonMappingException, JsonProcessingException
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
                if (detailed)
                {
                    nodesCountComparator.detailedValidation(alfrescoStoreId, k, uri, localName, fix);
                }
            }

        });
    }

}
