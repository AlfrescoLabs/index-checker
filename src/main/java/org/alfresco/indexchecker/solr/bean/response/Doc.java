package org.alfresco.indexchecker.solr.bean.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Doc
{
    public String id;
    public Object _version_;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("DBID")
    public int dbid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("ACLID")
    public int aclid;
    
    public int getDbid()
    {
        return dbid;
    }
    
    public int getAclid()
    {
        return aclid;
    }
}