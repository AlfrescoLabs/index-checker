package org.alfresco.indexchecker.solr.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Params
{
    public String q;
    @JsonProperty("facet.field")
    public String facetField;
    public String indent;
    public String facet;
    public String wt;
}