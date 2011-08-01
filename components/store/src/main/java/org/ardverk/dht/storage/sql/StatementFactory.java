package org.ardverk.dht.storage.sql;

import org.ardverk.dht.KUID;


class StatementFactory {

    public static final String PREFIX = "ardverk_";
    
    private static String pfx(String table) {
        return PREFIX + table;
    }
    
    public static final String BUCKETS = pfx("buckets");
    
    public static final String KEYS = pfx("keys");
    
    public static final String VALUES = pfx("values"); 
    
    public static final String PROPERTIES = pfx("properties");
    
    public static final String VALUE_SEQUENCE = pfx("value_sequence");
    
    public static final int BUCKETS_NAME_LENGTH = 16384;
    
    public static final int KEYS_URI_LENGTH = 16384;
    
    public static final int PROPERTIES_NAME_LENGTH = 256;
    
    public static final int PROPERTIES_VALUE_LENGTH = 16384;
    
    private final int length;
    
    public StatementFactory(int length) {
        this.length = length;
    }
    
    public String createBuckets() {
        return "CREATE TABLE " + BUCKETS + " ("
            + "id BINARY(" + length + ") PRIMARY KEY," // sha1(name) but we get it straight from the Key!
            + "name VARCHAR(" + BUCKETS_NAME_LENGTH + ") UNIQUE NOT NULL,"
            + "created DATETIME NOT NULL,"
            + "modified TIMESTAMP NOT NULL,"
            + ")";
    }
    
    public String createKeys() {
        return "CREATE TABLE " + KEYS + " ("
            + "id BINARY(" + length + ") PRIMARY KEY," // sha1(uri)
            + "bucketId BINARY(" + length + ") FOREIGN KEY REFERENCES " + BUCKETS + "(id),"
            + "uri VARCHAR(" + KEYS_URI_LENGTH + ") UNIQUE NOT NULL,"
            + "created DATETIME NOT NULL,"
            + "modified TIMESTAMP NOT NULL"
            + ")";
    }
    
    public String createValues() {
        return "CREATE TABLE " + VALUES + " ("
            + "id BINARY(" + length + ") PRIMARY KEY,"
            + "keyId BINARY(" + length + ") FOREIGN KEY REFERENCES " + KEYS + "(id),"
            + "created DATETIME NOT NULL,"
            + "tombstone DATETIME"
            + ")";
    }
    
    public String createProperties() {
        return "CREATE TABLE " + PROPERTIES + " ("
            + "id BIGINT PRIMARY KEY IDENTITY,"
            + "valueId BINARY(" + length + ") FOREIGN KEY REFERENCES " + VALUES + "(id),"
            + "name VARCHAR(" + PROPERTIES_NAME_LENGTH + ") NOT NULL,"
            + "value VARCHAR(" + PROPERTIES_VALUE_LENGTH + ") NOT NULL,"
            + ")";
    }
    
    public String upsertBuckets() {
        return "MERGE INTO " + BUCKETS + " USING (VALUES(CAST(? AS BINARY(" + length + ")), CAST(? AS VARCHAR(" + BUCKETS_NAME_LENGTH + ")), CAST(? AS DATETIME), CAST(? AS TIMESTAMP)))"
            + " AS vals(id, name, created, modified) ON " + BUCKETS + ".id = vals.id"
            + " WHEN MATCHED THEN UPDATE SET " + BUCKETS + ".modified = vals.modified"
            + " WHEN NOT MATCHED THEN INSERT VALUES vals.id, vals.name, vals.created, vals.modified";
    }
    
    public String upsertKeys() {
        return "MERGE INTO " + KEYS + " USING (VALUES(CAST(? AS BINARY(" + length + ")), CAST(? AS BINARY(" + length + ")), CAST(? AS VARCHAR(" + KEYS_URI_LENGTH + ")), CAST(? AS DATETIME), CAST(? AS TIMESTAMP)))"
            + " AS vals(id, bucketId, uri, created, modified) ON " + KEYS + ".id = vals.id"
            + " WHEN MATCHED THEN UPDATE SET " + KEYS + ".modified = vals.modified"
            + " WHEN NOT MATCHED THEN INSERT VALUES vals.id, vals.bucketId, vals.uri, vals.created, vals.modified";
    }
    
    public static final String BUCKET_MODIFIED = "UPDATE " + BUCKETS + " SET (modified = ?) WHERE id = ?";
    
    public static final String KEY_MODIFIED = "UPDATE " + KEYS + " SET (modified = ?) WHERE id = ?";
    
    public static final String INSERT_VALUE = "INSERT INTO " + VALUES + " NAMES(id, keyId, created) VALUES(?, ?, ?)";
    
    public static final String VALUE_DELETED = "UPDATE " + VALUES + " SET (tombstone = ?) WHERE id = ?";
    
    public static final String INSERT_PROPERTY = "INSERT INTO " + PROPERTIES + " NAMES(valueId, name, value) VALUES(?, ?, ?)";
    
    public static final String DELETE_PROPERTIES = "DELETE FROM " + PROPERTIES + " WHERE valueId = ?";
    
    public static final String VALUE_COUNT = "SELECT COUNT(id) FROM " + VALUES + " WHERE keyId = ?";
    
    public static enum Operation {
        LESS_THAN("<"),
        EQUAL_TO("="),
        GREATER_THAN(">"),
        LESS_THAN_OR_EQUAL_TO("<="),
        GREATER_THAN_OR_EQUAL_TO(">="),
        NOT_EQUAL_TO("<>");
        
        private final String value;
        
        private Operation(String value) {
            this.value = value;
        }
        
        public String stringValue() {
            return value;
        }
    }
    
    public static String getValues(KUID marker) {
        return getValues(marker != null ? Operation.GREATER_THAN_OR_EQUAL_TO : null);
    }
    
    public static String getValues(Operation operation) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT valueId, name, value FROM ").append(PROPERTIES)
            .append(" WHERE valueId IN (")
                .append("SELECT id FROM ").append(VALUES)
                .append(" WHERE keyId = ?");
        
        if (operation != null) {
            sb.append(" AND id ").append(operation.stringValue()).append("?");
        }
        
        sb.append(" ORDER BY id LIMIT ?)");
        
        return sb.toString();
    }
}
