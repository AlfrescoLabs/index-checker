package org.alfresco.indexchecker;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Compares the number of permissions in Alfresco DB and SOLR.
 * Logs missing permissions in Alfresco DB or SOLR if "report.detailed" properties has been set to true.
 */
@Service
public class PermissionsValidator
{
    
    static final Logger LOG = LoggerFactory.getLogger(App.class);
    
    @Autowired
    DbClient dbClient;
    
    @Autowired
    SolrWebClient solrWebClient;
    
    @Autowired
    PermissionsCountComparator permissionsCountComparator;
    
    /**
     * Logs the number of permissions in Alfresco DB and SOLR.
     * If detailed, logs the missing nodes in Alfresco DB or SOLR.
     * @param detailed
     */
    public void validate(boolean detailed) throws JsonMappingException, JsonProcessingException
    {
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
            if (detailed)
            {
                permissionsCountComparator.logDetailedReport();    
            }
        }
    }

}
