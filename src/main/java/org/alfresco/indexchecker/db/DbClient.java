package org.alfresco.indexchecker.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
            + "WHERE AN.store_id = %s";
    static final String SQL_QUERY_COUNT_UNINDEXED_NODES = 
            "SELECT COUNT(1) "
            + "FROM alf_node, alf_node_properties, alf_qname, alf_namespace " 
            + "WHERE alf_node_properties.node_id = alf_node.id "
            + "  AND alf_node.store_id = %s "
            + "  AND alf_node_properties.qname_id = alf_qname.id " 
            + "  AND alf_qname.ns_id = alf_namespace.id "
            + "  AND NOT alf_node_properties.boolean_value " 
            + "  AND alf_qname.local_name = 'isIndexed' "
            + "  AND alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0'";
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
                - jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_UNINDEXED_NODES, storeId), Integer.class)
                - jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_DELETES, storeId), Integer.class);
    }
    
    static final String SQL_QUERY_MAX_NODES = 
            "SELECT MAX(AN.id) "
            + "FROM alf_node AN "
            + "WHERE AN.store_id = %s";
    public Integer getMaxDbId(Integer storeId)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_QUERY_MAX_NODES, storeId), Integer.class);
    }
    
    static final String SQL_QUERY_COUNT_BY_TYPE = 
            "SELECT COUNT(1) " 
            + "FROM alf_node AN, alf_qname, alf_namespace "
            + "WHERE AN.type_qname_id = alf_qname.id " 
            + "  AND AN.store_id = %s"
            + "  AND alf_qname.ns_id = alf_namespace.id " 
            + "  AND alf_qname.local_name = '%s' "
            + "  AND alf_namespace.uri = '%s'" 
            + "  AND NOT EXISTS (select alf_node_properties.node_id "
            + "    from alf_node_properties, alf_qname, alf_namespace "
            + "    where alf_node_properties.node_id = AN.id "
            + "    and alf_node_properties.qname_id = alf_qname.id " 
            + "    and alf_qname.ns_id = alf_namespace.id "
            + "    and NOT alf_node_properties.boolean_value " 
            + "    and alf_qname.local_name = 'isIndexed' "
            + "    and alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0')";
    public Integer getCountByType(Integer storeId, String uri, String localName)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_QUERY_COUNT_BY_TYPE, storeId, localName, uri),
                Integer.class);
    }
    
    static final String SQL_QUERY_MAX_BY_TYPE = 
            "SELECT MAX(AN.id) " 
            + "FROM alf_node AN, alf_qname, alf_namespace "
            + "WHERE AN.type_qname_id = alf_qname.id " 
            + "  AND AN.store_id = %s"
            + "  AND alf_qname.ns_id = alf_namespace.id " 
            + "  AND alf_qname.local_name = '%s' "
            + "  AND alf_namespace.uri = '%s'" 
            + "  AND NOT EXISTS (select alf_node_properties.node_id "
            + "    from alf_node_properties, alf_qname, alf_namespace "
            + "    where alf_node_properties.node_id = AN.id "
            + "    and alf_node_properties.qname_id = alf_qname.id " 
            + "    and alf_qname.ns_id = alf_namespace.id "
            + "    and NOT alf_node_properties.boolean_value " 
            + "    and alf_qname.local_name = 'isIndexed' "
            + "    and alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0')";
    public Integer getMaxByType(Integer storeId, String uri, String localName)
    {
        return jdbcTemplate.queryForObject(String.format(SQL_QUERY_MAX_BY_TYPE, storeId, localName, uri),
                Integer.class);
    }

    static final String SQL_QUERY_ID_LIST_BY_TYPE = 
            "SELECT AN.id " 
            + "FROM alf_node AN, alf_qname, alf_namespace "
            + "WHERE AN.type_qname_id = alf_qname.id " 
            + "  AND AN.store_id = %s"
            + "  AND AN.id >= %s"
            + "  AND AN.id <= %s"
            + "  AND alf_qname.ns_id = alf_namespace.id " 
            + "  AND alf_qname.local_name = '%s' "
            + "  AND alf_namespace.uri = '%s'" 
            + "  AND NOT EXISTS (select alf_node_properties.node_id "
            + "    from alf_node_properties, alf_qname, alf_namespace "
            + "    where alf_node_properties.node_id = AN.id "
            + "    and alf_node_properties.qname_id = alf_qname.id " 
            + "    and alf_qname.ns_id = alf_namespace.id "
            + "    and NOT alf_node_properties.boolean_value " 
            + "    and alf_qname.local_name = 'isIndexed' "
            + "    and alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0')";
    public List<Integer> getIdListByType(Integer storeId, Integer minId, Integer maxId, String uri, String localName)
    {
        return jdbcTemplate.query(String.format(SQL_QUERY_ID_LIST_BY_TYPE, storeId, minId, maxId, localName, uri),
                new RowMapper<Integer>()
                {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException
                    {
                        return rs.getInt(1);
                    }
                });
    }
    
    static final String SQL_QUERY_COUNT_ACLS = 
            "SELECT count(1) " 
            + "FROM " 
            + "( " 
            + "  SELECT acl.id " 
            + "  FROM alf_access_control_list acl " 
            + "  WHERE EXISTS (select 1 from alf_node admnode where admnode.acl_id = acl.id) " 
            + "  UNION " 
            + "  SELECT distinct(acl.inherited_acl) " 
            + "  FROM alf_access_control_list acl " 
            + "  WHERE EXISTS (select 1 from alf_node admnode where admnode.acl_id = acl.id) " 
            + "    AND acl.inherited_acl IS NOT NULL " 
            + ") SubQuery";
    public Integer getAclCount()
    {
        return jdbcTemplate.queryForObject(SQL_QUERY_COUNT_ACLS, Integer.class);
    }
    
    static final String SQL_QUERY_ACL_LIST =
            "SELECT acl.id "
            + "FROM alf_access_control_list acl "
            + "WHERE EXISTS (select 1 from alf_node admnode where admnode.acl_id = acl.id) "
            + "  AND acl.id >= %s "
            + "  AND acl.id <= %s "
            + "UNION "
            + "SELECT distinct(acl.inherited_acl) "
            + "  FROM alf_access_control_list acl "
            + " WHERE EXISTS (select 1 from alf_node admnode where admnode.acl_id = acl.id) "
            + "   AND acl.inherited_acl IS NOT NULL "
            + "   AND acl.inherited_acl >= %s "
            + "   AND acl.inherited_acl <= %s";
    public List<Integer> getAclList(Integer minAclId, Integer maxAclId)
    {
        return jdbcTemplate.query(String.format(SQL_QUERY_ACL_LIST, minAclId, maxAclId, minAclId, maxAclId),
                new RowMapper<Integer>()
                {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException
                    {
                        return rs.getInt(1);
                    }
                });
    }
    
    static final String SQL_QUERY_MAX_ACL = 
            "SELECT max(id) "
            + "FROM alf_access_control_list";
    public Integer getMaxAclId()
    {
        return jdbcTemplate.queryForObject(SQL_QUERY_MAX_ACL, Integer.class);
    }
}
