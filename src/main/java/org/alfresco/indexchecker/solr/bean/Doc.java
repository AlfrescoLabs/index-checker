package org.alfresco.indexchecker.solr.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Doc
{
    public String id;
    @JsonProperty("DBID")
    public int dbid;
    public Object _version_;
}