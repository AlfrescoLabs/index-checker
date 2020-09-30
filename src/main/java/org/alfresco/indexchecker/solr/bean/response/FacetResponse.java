package org.alfresco.indexchecker.solr.bean.response;

/**
 * Root Jackson Bean to be used to unmarshall Facet Queries to SOLR.
 */
public class FacetResponse
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
    public FacetCounts facet_counts;
    public boolean processedDenies;
}