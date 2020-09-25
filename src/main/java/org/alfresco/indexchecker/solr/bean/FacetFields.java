package org.alfresco.indexchecker.solr.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacetFields
{
    @JsonProperty("TYPE")
    public List<Object> type;
}