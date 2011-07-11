package org.ardverk.dht.storage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyFactory;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;


public class Index implements Closeable {

    private static final String CREATE_KEYS
        = "CREATE TABLE keys ("
        + "id BINARY(20) PRIMARY KEY," // sha1(key)
        + "key VARCHAR(16384) UNIQUE NOT NULL,"
        + "created DATETIME NOT NULL,"
        + "modified TIMESTAMP NOT NULL"
        + ")";
    
    private static final String CREATE_VALUES
        = "CREATE TABLE entries (" // Using 'entries' because 'values' is a reserved keyword
        + "id BINARY(20) PRIMARY KEY,"
        + "kid BINARY(20) FOREIGN KEY REFERENCES keys(id),"
        + "created DATETIME NOT NULL"
        + ")";
    
    private static final String CREATE_PROPERTIES 
        = "CREATE TABLE properties ("
        + "id BIGINT PRIMARY KEY IDENTITY,"
        + "vid BINARY(20) FOREIGN KEY REFERENCES entries(id),"
        + "name VARCHAR(256) NOT NULL,"
        + "value VARCHAR(16384) NOT NULL,"
        + ")";
    
    public static Index create(File dir) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            
            String url = "jdbc:hsqldb:mem:index";
            String user = "sa";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);
            
            Statement statement = connection.createStatement();
            statement.execute(CREATE_KEYS);
            statement.execute(CREATE_VALUES);
            statement.execute(CREATE_PROPERTIES);
            statement.close();
            
            return new Index(connection);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final Connection connection;
    
    private Index(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException err) {
            throw new IOException("SQLException", err);
        }
    }
    
    public boolean containsKey(Key key) throws SQLException {
        String path = key.getPath();
        byte[] kid = hash(path);
        return containsKey(kid);
    }
    
    private boolean containsKey(byte[] kid) throws SQLException {
        return contains("SELECT COUNT(id) FROM keys WHERE id = ?", kid);
    }
    
    public boolean containsValue(KUID valueId) throws SQLException {
        return containsValue(valueId.getBytes(false));
    }
    
    private boolean containsValue(byte[] vid) throws SQLException {
        return contains("SELECT COUNT(id) FROM entries WHERE id = ?", vid);
    }
    
    private boolean contains(String sql, byte[] key) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            ps.setBytes(1, key);
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return 0 < rs.getInt(1);
                }
            } finally {
                close(rs);
            }
        } finally {
            close(ps);
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public Map.Entry<KUID, Context>[] get(Key key) throws SQLException {
        String path = key.getPath();
        byte[] kid = hash(path);
        
        PreparedStatement ps 
            = connection.prepareStatement(
                "SELECT e.id, p.name, p.value FROM entries e, properties p WHERE e.kid = ? AND e.id = p.vid");
        try {
            ps.setBytes(1, kid);
            
            ResultSet rs = ps.executeQuery();
            
            try {
                if (rs.next()) {
                    
                    Map<KUID, Context> map = new HashMap<KUID, Context>();
                    Context context = null;
                    
                    byte[] current = null;
                    
                    do {
                        byte[] vid = rs.getBytes(1);
                        
                        if (!Arrays.equals(current, vid)) {
                            context = new Context();
                            map.put(KUID.create(vid), context);
                        }
                        
                        String name = rs.getString(2);
                        String value = rs.getString(3);
                        context.addHeader(name, value);
                    } while (rs.next());
                    
                    Set<Map.Entry<KUID, Context>> entries = map.entrySet();
                    return CollectionUtils.toArray(entries, Map.Entry.class);
                }
                
                return null;
            } finally {
                close(rs);
            }
        } finally {
            close(ps);
        }
    }
    
    public Context get(KUID valueId) throws SQLException {
        byte[] vid = valueId.getBytes();
        
        PreparedStatement ps 
            = connection.prepareStatement(
                "SELECT name, value FROM properties WHERE vid = ?");
        try {
            ps.setBytes(1, vid);
            
            ResultSet rs = ps.executeQuery();
            try {
                Context context = null;
                
                if (rs.next()) {
                    context = new Context();
                    
                    do {
                        String name = rs.getString(1);
                        String value = rs.getString(2);
                        context.addHeader(name, value);
                    } while (rs.next());
                }
                
                return context;
            } finally {
                close(rs);
            }
        } finally {
            close(ps);
        }
    }
    
    public void add(Key key, Context context, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        
        Date created = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        String path = key.getPath();
        byte[] kid = hash(path);
        
        try {
            connection.setAutoCommit(false);
            
            // KEYS
            {
                PreparedStatement ps = null;
                try {
                    if (!containsKey(kid)) {
                        ps = connection.prepareStatement(
                                "INSERT INTO keys NAMES(id, key, created, modified) VALUES(?, ?, ?, ?)");
                        ps.setBytes(1, kid);
                        ps.setString(2, path);
                        ps.setDate(3, created);
                        ps.setTimestamp(4, modified);
                    } else {
                        ps = connection.prepareStatement(
                                "UPDATE keys SET modified = ? WHERE id = ?");
                        
                        ps.setTimestamp(1, modified);
                        ps.setBytes(2, kid);
                    }
                    
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
            }
            
            byte[] vid = valueId.getBytes(false);
            
            // ENTRIES
            {
                PreparedStatement ps 
                    = connection.prepareStatement(
                        "INSERT INTO entries NAMES(id, kid, created) VALUES(?, ?, ?)");
                try {
                    ps.setBytes(1, vid);
                    ps.setBytes(2, kid);
                    ps.setDate(3, created);
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
            }
            
            // PROPERTIES
            {
                PreparedStatement ps 
                    = connection.prepareStatement(
                        "INSERT INTO properties NAMES(vid, name, value) VALUES(?, ?, ?)");
                try {
                    for (Header header : context.getHeaders()) {
                        ps.setBytes(1, vid);
                        ps.setString(2, header.getName());
                        ps.setString(3, header.getValue());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                } finally {
                    close(ps);
                }
            }
            
            connection.commit();
            
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private static byte[] hash(String value) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        return md.digest(StringUtils.getBytes(value));
    }
    
    public static void main(String[] args) throws Exception {
        Index index = Index.create(null);
        
        Key key = KeyFactory.parseKey("ardverk:///hello/world");
        
        for (int i = 0; i < 10; i++) {
            KUID valueId = KUID.createRandom(key.getId());
            
            Context context = new Context();
            context.addHeader("name1-" + i, "value1");
            context.addHeader("name2-" + i, "value2");
            
            index.add(key, context, valueId);
            
            
            System.out.println(index.get(valueId));
        }
        
        System.out.println(Arrays.toString(index.get(key)));
    }
    
    private static void close(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException err) {}
        }
    }
    
    private static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException err) {}
        }
    }
}
