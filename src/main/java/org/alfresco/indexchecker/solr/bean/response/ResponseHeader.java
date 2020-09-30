package org.alfresco.indexchecker.solr.bean.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseHeader
{
    public int status;
    @JsonProperty("QTime")
    public int qTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Params params;
}