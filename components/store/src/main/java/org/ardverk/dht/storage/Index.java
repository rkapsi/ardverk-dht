package org.ardverk.dht.storage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        + "key VARCHAR(16384) UNIQUE NOT NULL"
        + ")";
    
    private static final String CREATE_VALUES
        = "CREATE TABLE entries (" // Using 'entries' because 'values' is a reserved keyword
        + "id BINARY(20) PRIMARY KEY,"
        + "kid BINARY(20) FOREIGN KEY REFERENCES keys(id)"
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
    
    public Context[] get(Key key) throws SQLException {
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
                    
                    List<Context> list = new ArrayList<Context>();
                    Context context = null;
                    
                    byte[] current = null;
                    
                    do {
                        byte[] id = rs.getBytes(1);
                        
                        if (!Arrays.equals(current, id)) {
                            context = new Context();
                            list.add(context);
                        }
                        
                        String name = rs.getString(2);
                        String value = rs.getString(3);
                        context.addHeader(name, value);
                    } while (rs.next());
                    
                    return CollectionUtils.toArray(list, Context.class);
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
        String path = key.getPath();
        byte[] kid = hash(path);
        
        PreparedStatement keys 
            = connection.prepareStatement(
                    "INSERT INTO keys NAMES(id, key) VALUES(?, ?)");
        try {
            keys.setBytes(1, kid);
            keys.setString(2, path);
            keys.executeUpdate();
        } catch (SQLException err) {
            // IGNORE
        } finally {
            close(keys);
        }
        
        byte[] vid = valueId.getBytes();
        
        PreparedStatement values 
            = connection.prepareStatement(
                "INSERT INTO entries NAMES(id, kid) VALUES(?, ?)");
        try {
            values.setBytes(1, vid);
            values.setBytes(2, kid);
            values.executeUpdate();
        } finally {
            close(values);
        }
        
        PreparedStatement properties 
            = connection.prepareStatement(
                "INSERT INTO properties NAMES(vid, name, value) VALUES(?, ?, ?)");
        try {
            for (Header header : context.getHeaders()) {
                properties.setBytes(1, vid);
                properties.setString(2, header.getName());
                properties.setString(3, header.getValue());
                properties.addBatch();
            }
            
            properties.executeBatch();
        } finally {
            close(properties);
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
