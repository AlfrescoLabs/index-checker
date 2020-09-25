package org.alfresco.indexchecker.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Alfresco DB Client
 * Direct access to server and port using JDBC is required.
 */
@Service
public class DbClient
{
    
    /**
     * Working space protocol and identifier.
     * This STORE is indexed by ALFRESCO Solr core.
     */
    public static final String ALFRESCO_STORE_PROTOCOL = "workspace";
    public static final String ALFRESCO_STORE_IDENTIFIER = "SpacesStore";
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    static final String SQL_GET_STORE_ID = 
            "SELECT id FROM alf_store WHERE protocol='%s' AND identifier='%s'";
    public Integer getStoreId(String protocol, String identifier)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_GET_STORE_ID, protocol, identifier), Integer.class);
    }
    
    static final String SQL_QUERY_COUNT_NODES = 
            "SELECT COUNT(1) "
            + "FROM alf_node AN "
            + "WHERE AN.store_id = %s "
            + "AND NOT EXISTS (select alf_node_properties.node_id "
            + "  from alf_node_properties, alf_qname, alf_namespace " 
            + "  where alf_node_properties.node_id = AN.id "
            + "  and alf_node_properties.qname_id = alf_qname.id " 
            + "  and alf_qname.ns_id = alf_namespace.id "
            + "  and NOT alf_node_properties.boolean_value " 
            + "  and alf_qname.local_name = 'isIndexed' "
            + "  and alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0')";
    static final String SQL_QUERY_COUNT_DELETES = 
            "SELECT COUNT(1) " 
            + "FROM alf_node, alf_qname, alf_namespace "
            + "WHERE alf_node.type_qname_id = alf_qname.id " 
            + "AND alf_node.store_id = %s "
            + "AND alf_qname.ns_id = alf_namespace.id " 
            + "AND alf_qname.local_name = 'deleted' "
            + "AND alf_namespace.uri = 'http://www.alfresco.org/model/system/1.0'";
    public Integer getNodeCount(Integer storeId)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_NODES, storeId), Integer.class)
                - jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_DELETES, storeId), Integer.class);
    }
    
    static final String SQL_QUERY_COUNT_BY_TYPE = 
            "SELECT COUNT(1) " 
            + "FROM alf_node AN, alf_qname, alf_namespace "
            + "WHERE AN.type_qname_id = alf_qname.id " 
            + "  AND AN.store_id = %s"
            + "  AND alf_qname.ns_id = alf_namespace.id " 
            + "  AND alf_qname.local_name = '%s' "
            + "  AND alf_namespace.uri = '%s'" 
            + "  AND NOT EXISTS (select alf_node_properties.node_id\n"
            + "    from alf_node_properties, alf_qname, alf_namespace\n"
            + "    where alf_node_properties.node_id = AN.id\n"
            + "    and alf_node_properties.qname_id = alf_qname.id\n" 
            + "    and alf_qname.ns_id = alf_namespace.id\n"
            + "    and NOT alf_node_properties.boolean_value\n" 
            + "    and alf_qname.local_name = 'isIndexed'\n"
            + "    and alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0')";
    public Integer getCountByType(Integer storeId, String uri, String localName)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_BY_TYPE, storeId, localName, uri),
                Integer.class);
    }

}
