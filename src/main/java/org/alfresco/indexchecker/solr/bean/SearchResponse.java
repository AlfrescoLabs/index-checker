package org.alfresco.indexchecker.solr.bean;

/**
 * Root Jackson Bean to be used to unmarshall Search Queries to SOLR.
 */
public class SearchResponse
{
    public ResponseHeader responseHeader;
    public OriginalParameters _original_parameters_;
    public FieldMappings _field_mappings_;
    public DateMappings _date_mappings_;
    public RangeMappings _range_mappings_;
    public PivotMappings _pivot_mappings_;
    public IntervalMappings _interval_mappings_;
    public StatsFieldMappings _stats_field_mappings_;
    public StatsFacetMappings _stats_facet_mappings_;
    public FacetFunctionMappings _facet_function_mappings_;
    public int lastIndexedTx;
    public long lastIndexedTxTime;
    public int txRemaining;
    public Response response;
    public boolean processedDenies;
}