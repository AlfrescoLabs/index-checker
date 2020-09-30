package org.alfresco.indexchecker;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.indexchecker.db.DbClient;
import org.alfresco.indexchecker.solr.SolrWebClient;
import org.alfresco.indexchecker.solr.bean.response.Doc;
import org.alfresco.indexchecker.solr.bean.response.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Compare node existence in SOLR and Alfresco DB.
 */
@Service
public class NodesCountComparator
{
    
    static final Logger LOG = LoggerFactory.getLogger(NodesCountComparator.class);
    
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
     * Apply fix actions to missing nodes if "fix" is set to true
     * 
     * @param alfrescoStoreId DB Id for Alfresco Store (for instance workspace://SpacesStore) 
     * @param type Complete name of a Content Model Type
     * @param uri Uri for the Content Model Type
     * @param localName Local name for the Content Model Type
     * @param fix apply fix actions when "true"
     */
    public void detailedValidation(Integer alfrescoStoreId, String type, String uri, String localName, boolean fix)
    {
        // Max dbId in the database for selected Alfresco Store
        Integer dbIdMax = dbClient.getMaxDbId(alfrescoStoreId);
        
        Integer currentBatchId = 0;
        Integer maxSolrDbId = 0;
        
        while (maxSolrDbId < dbIdMax)
        {
            try
            {
                
                SearchResponse searchResponse = solrWebClient.getDbIdRangeByType(SolrWebClient.ALFRESCO_CORE_NAME, type,
                        currentBatchId);
                List<Integer> solrIds = searchResponse.response.docs.stream().map(Doc::getDbid)
                        .collect(Collectors.toList());

                if (solrIds.size() == 0)
                {
                    Integer maxDbId = dbClient.getMaxByType(alfrescoStoreId, uri, localName);
                    if (maxSolrDbId < maxDbId)
                    {
                        LOG.error(
                                "TYPE {}: SOLR max DbId is {} while DB has a maximum of {}, processing index is still progressing",
                                type, maxSolrDbId, maxDbId);
                    }
                    maxSolrDbId = Integer.MAX_VALUE;
                    break;
                }
                
                int minDbId = solrIds.get(0);
                int maxDbId = solrIds.get(solrIds.size() - 1);
                maxSolrDbId = maxDbId;

                LOG.debug("TYPE {}: Processing DbIds from {} to {}", type, minDbId, maxDbId);

                List<Integer> dbIds = dbClient.getIdListByType(alfrescoStoreId, minDbId, maxDbId, uri, localName);

                List<Integer> missingDbIds = dbIds.stream()
                        .filter(dbId -> !solrIds.contains(dbId))
                        .collect(Collectors.toList());                
                
                if (missingDbIds.size() > 0)
                {
                    LOG.error("TYPE {}: DbIds present in DB but missed in SOLR {}", type, missingDbIds);
                    if (fix)
                    {
                        missingDbIds.parallelStream().forEach(nodeId -> {
                            LOG.debug("TYPE {}: Reindexing document with DBID {} in Solr Index", type, nodeId);
                            try
                            {
                                solrWebClient.reindexById(
                                        SolrWebClient.ALFRESCO_CORE_NAME,
                                        SolrWebClient.NODE_ID_PARAM_NAME,
                                        nodeId);
                            }
                            catch (Exception e)
                            {
                                LOG.error("Some error happened when reindexing Solr Document with DBID {}. Error message: ", nodeId,
                                        e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                }
                
                List<Integer> missingSolrIds = solrIds.stream()
                        .filter(solrId -> !dbIds.contains(solrId))
                        .collect(Collectors.toList());
                
                if (missingSolrIds.size() > 0)
                {
                    LOG.error("TYPE {}: DbIds present in SOLR but missed in DB {}", type, missingSolrIds);
                    if (fix)
                    {
                        missingSolrIds.parallelStream().forEach(nodeId -> {
                            LOG.debug("TYPE {}: Deleting document with DBID {} from Solr Index", type, nodeId);
                            try
                            {
                                solrWebClient.deleteById(
                                        SolrWebClient.ALFRESCO_CORE_NAME,
                                        SolrWebClient.NODE_ID_FIELD_NAME, 
                                        nodeId);
                            }
                            catch (Exception e)
                            {
                                LOG.error("Some error happened when deleting Solr Document with DBID {}. Error message: ", nodeId,
                                        e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                }

            }
            catch (Exception e)
            {
                LOG.error("Some error happened when processing detailed report for TYPE {}. Error message: ", type,
                        e.getMessage());
                e.printStackTrace();
                break;
            }
            currentBatchId = currentBatchId + BATCH_SIZE;
        }
    }

}
