package org.alfresco.indexchecker;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.Doc;
import org.alfresco.indexchecker.solr.bean.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Compare permission existence in SOLR and Alfresco DB.
 */
@Service
public class PermissionsCountComparator
{

    static final Logger LOG = LoggerFactory.getLogger(PermissionsCountComparator.class);

    /**
     * Number of SOLR Documents taken on every request to be compared with DB
     */
    public static final Integer BATCH_SIZE = 1000;

    @Autowired
    DbClient dbClient;

    @Autowired
    SolrWebClient solrWebClient;

    /**
     * Logs details for the comparing process between SOLR and Alfresco DB
     */
    public void logDetailedReport()
    {
        // Max aclId in the database
        Integer maxDbAclId = dbClient.getMaxAclId();

        Integer currentBatchId = 0;
        Integer maxSolrAclId = 0;

        while (maxSolrAclId < maxDbAclId)
        {
            try
            {

                SearchResponse searchResponse = solrWebClient.getAclIdRange(SolrWebClient.ALFRESCO_CORE_NAME,
                        currentBatchId);
                List<Integer> solrIds = searchResponse.response.docs.stream().map(Doc::getAclid)
                        .collect(Collectors.toList());
                
                if (solrIds.size() == 0)
                {
                    if (maxSolrAclId < maxDbAclId)
                    {
                        LOG.error(
                                "SOLR max AclDb is {} while DB has a maximum of {}, processing index is still progressing",
                                maxSolrAclId, maxDbAclId);
                    }
                    maxSolrAclId = Integer.MAX_VALUE;
                    break;
                }

                int minAclId = solrIds.get(0);
                int maxAclId = solrIds.get(solrIds.size() - 1);
                maxSolrAclId = maxAclId;

                LOG.debug("TYPE {}: Processing AclIds from {} to {}", minAclId, maxAclId);

                List<Integer> dbIds = dbClient.getAclList(minAclId, maxAclId);

                List<Integer> missingDbIds = dbIds.stream().filter(dbId -> !solrIds.contains(dbId))
                        .collect(Collectors.toList());

                if (missingDbIds.size() > 0)
                {
                    LOG.error("AclIds present in DB but missed in SOLR {}", missingDbIds);
                }

                List<Integer> missingSolrIds = solrIds.stream().filter(solrId -> !dbIds.contains(solrId))
                        .collect(Collectors.toList());

                if (missingSolrIds.size() > 0)
                {
                    LOG.error("AclIds present in SOLR but missed in DB {}", missingSolrIds);
                }

            }
            catch (Exception e)
            {
                LOG.error("Some error happened when processing detailed report for ACLs. Error message: ", 
                        e.getMessage());
                e.printStackTrace();
                break;
            }
            currentBatchId = currentBatchId + BATCH_SIZE;
        }
    }

}
