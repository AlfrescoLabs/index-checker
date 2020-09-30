package org.alfresco.indexchecker.solr.bean.response;

/**
 * Root Jackson Bean to be used to unmarshall Action Response from SOLR.
 */
public class ActionResponse
{
    public ResponseHeader responseHeader;
    public Action action;
}
