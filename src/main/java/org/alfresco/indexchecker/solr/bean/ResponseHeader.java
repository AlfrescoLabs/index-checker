package org.alfresco.indexchecker.solr.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseHeader
{
    public int status;
    @JsonProperty("QTime")
    public int qTime;
    public Params params;
}