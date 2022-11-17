package org.alfresco.indexchecker.solr;

import org.alfresco.indexchecker.solr.bean.request.Delete;
import org.alfresco.indexchecker.solr.bean.request.DeleteRequest;
import org.alfresco.indexchecker.solr.bean.response.ActionResponse;
import org.alfresco.indexchecker.solr.bean.response.FacetResponse;
import org.alfresco.indexchecker.solr.bean.response.SearchResponse;
import org.alfresco.indexchecker.solr.bean.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * Alfresco SOLR Client
 * Direct access to server and port using HTTP is required.
 */
@Service
public class SolrWebClient
{
    
    public static final String ALFRESCO_CORE_NAME = "alfresco";

    @Value("${solr.url}")
    String solrServerUrl;

    @Value("${validation.nodes.batch.size}")
    Integer nodesBatchSize;
    
    @Value("${validation.permissions.batch.size}")
    Integer permissionsBatchSize;

    @Autowired
    SpringWebClient springWebClient;

    /**
     * Get a count of SOLR Documents by TYPE using facet query
     * @param core Core name: alfresco, archive
     * @return Facets including TYPE QName and the count of Documents for the type
     */
    public FacetResponse getDocumentCountByType(String core) throws JsonMappingException, JsonProcessingException
    {

        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "*")
                        .queryParam("facet.field", "TYPE")
                        .queryParam("facet", "on")
                        .queryParam("wt", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), FacetResponse.class);

    }
    
    /**
     * Get a list of dbIds for the specified type starting from a given dbId and getting BATCH_SIZE results
     * @param core Core name: alfresco, archive
     * @param type Complete name of a Content Model Type
     * @param minDbId Starting number for dbId (included) to get results from 
     * @return List of dbIds for the specified type
     */
    public SearchResponse getDbIdRangeByType(String core, String type, Integer minDbId) throws JsonMappingException, JsonProcessingException
    {
        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "{query}")
                        .queryParam("start", minDbId)
                        .queryParam("rows", nodesBatchSize)
                        .queryParam("sort", "DBID asc")
                        .queryParam("wt", "json")
                        .build("{!term f=TYPE}" + type))
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), SearchResponse.class);
    }
    
    /**
     * Gets all the ACLs indexed in a SOLR Core
     * @param core Core name: alfresco, archive
     * @return List of aclIds for the specified core
     */
    public SearchResponse getAclCount(String core) throws JsonMappingException, JsonProcessingException
    {
        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "{query}")
                        .queryParam("wt", "json")
                        .build("{!term f=DOC_TYPE}Acl"))
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), SearchResponse.class);
    }
    
    /**
     * Gets a list of aclIds for the specified core from a given aclId and getting BATCH_SIZE results
     * @param core Core name: alfresco, archive
     * @param minAclId Starting number for aclId (included) to get results from
     * @return List of aclIds for the specified type
     */
    public SearchResponse getAclIdRange(String core, Integer minAclId) throws JsonMappingException, JsonProcessingException
    {
        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "{query}")
                        .queryParam("fl", "{cached}")
                        .queryParam("start", minAclId)
                        .queryParam("rows", permissionsBatchSize)
                        .queryParam("sort", "ACLID asc")
                        .queryParam("wt", "json")
                        .build("{!term f=DOC_TYPE}Acl", "[cached]ACLID, id, _version_"))
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), SearchResponse.class);
    }
    
    public static final String NODE_ID_FIELD_NAME = "DBID";
    public static final String ACL_ID_FIELD_NAME = "ACLID";
    /**
     * Delete a document from SOLR Index by type (NODE or ACL)
     * @param core Core name: alfresco, archive
     * @param fieldName Id Field Name: NODE_ID_FIELD_NAME or ACL_ID_FIELD_NAME
     * @param id Number for the DBID or ACLID to be deleted
     */
    public UpdateResponse deleteById(String core, String fieldName, Integer id) throws JsonMappingException, JsonProcessingException
    {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.delete = new Delete(fieldName + ":" + id);
        
        
        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .post()
                .uri(builder -> builder.path("/" + core + "/update")
                        .queryParam("commit", "true")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(deleteRequest), DeleteRequest.class)
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), UpdateResponse.class);
    }
    
    public static final String NODE_ID_PARAM_NAME = "nodeid";
    public static final String ACL_ID_PARAM_NAME = "aclid";
    /**
     * Reindex a document in SOLR Index by type (NODE or ACL)
     * @param core Core name: alfresco, archive
     * @param paramName Param Name: NODE_ID_PARAM_NAME or ACL_ID_PARAM_NAME
     * @param id Number for the DBID or ACLID to be reindexed
     */
    public ActionResponse reindexById(String core, String paramName, Integer id) throws JsonMappingException, JsonProcessingException
    {
        return new ObjectMapper().readValue(springWebClient.getWebClient(solrServerUrl)
                .get()
                .uri(builder -> builder.path("/admin/cores")
                        .queryParam("core", core)
                        .queryParam("action", "reindex")
                        .queryParam(paramName, id)
                        .queryParam("wt", "json")
                        .build())
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), ActionResponse.class);
    }
    
    
}
