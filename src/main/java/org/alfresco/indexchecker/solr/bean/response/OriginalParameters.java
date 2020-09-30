package org.alfresco.indexchecker.solr.bean.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OriginalParameters
{
    public String q;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("facet.field")
    public String facetField;
    public String defType;
    public String df;
    public String indent;
    public String echoParams;
    public String rows;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String facet;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String start;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String sort;
    public String wt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String f;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String fl;
}