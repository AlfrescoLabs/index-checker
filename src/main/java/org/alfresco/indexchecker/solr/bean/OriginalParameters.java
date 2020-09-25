package org.alfresco.indexchecker.solr.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OriginalParameters
{
    public String q;
    @JsonProperty("facet.field")
    public String facetField;
    public String defType;
    public String df;
    public String indent;
    public String echoParams;
    public String rows;
    public String facet;
    public String wt;
}