package org.alfresco.indexchecker.solr;

import org.alfresco.indexchecker.solr.bean.FacetResponse;
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
    
}
