package org.alfresco.indexchecker.solr;

import org.alfresco.indexchecker.NodesCountComparator;
import org.alfresco.indexchecker.PermissionsCountComparator;
import org.alfresco.indexchecker.solr.bean.FacetResponse;
import org.alfresco.indexchecker.solr.bean.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    /**
     * Get a count of SOLR Documents by TYPE using facet query
     * @param core Core name: alfresco, archive
     * @return Facets including TYPE QName and the count of Documents for the type
     */
    public FacetResponse getDocumentCountByType(String core) throws JsonMappingException, JsonProcessingException
    {

        return new ObjectMapper().readValue(WebClient.create(solrServerUrl).get()
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
        return new ObjectMapper().readValue(WebClient.create(solrServerUrl).get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "{query}")
                        .queryParam("start", minDbId)
                        .queryParam("rows", NodesCountComparator.BATCH_SIZE)
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
        return new ObjectMapper().readValue(WebClient.create(solrServerUrl).get()
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
        return new ObjectMapper().readValue(WebClient.create(solrServerUrl).get()
                .uri(builder -> builder.path("/" + core + "/select")
                        .queryParam("q", "{query}")
                        .queryParam("fl", "{cached}")
                        .queryParam("start", minAclId)
                        .queryParam("rows", PermissionsCountComparator.BATCH_SIZE)
                        .queryParam("sort", "ACLID asc")
                        .queryParam("wt", "json")
                        .build("{!term f=DOC_TYPE}Acl", "[cached]ACLID, id, _version_"))
                .accept(MediaType.APPLICATION_JSON).exchange()
                .flatMap(res -> res.bodyToMono(String.class))
                .block(), SearchResponse.class);
    }
    
}
