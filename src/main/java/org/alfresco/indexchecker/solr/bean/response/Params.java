package org.alfresco.indexchecker.solr.bean.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Params
{
    public String q;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("facet.field")
    public String facetField;
    public String indent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String facet;
    public String wt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String start;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String rows;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String sort;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String f;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String fl;
}