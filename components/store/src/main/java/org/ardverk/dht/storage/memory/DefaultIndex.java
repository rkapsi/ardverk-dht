package org.ardverk.dht.storage.memory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.DefaultKey;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyFactory;
import org.ardverk.dht.storage.message.Context;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.DefaultEntry;
import org.ardverk.utils.StringUtils;


public class DefaultIndex extends AbstractIndex {

    private static final String CREATE_BUCKETS
        = "CREATE TABLE buckets ("
        + "id BINARY(20) PRIMARY KEY," // sha1(name) but we get it straight from the Key!
        + "name VARCHAR(16384) UNIQUE NOT NULL,"
        + "created DATETIME NOT NULL,"
        + "modified TIMESTAMP NOT NULL,"
        + ")";
    
    private static final String CREATE_KEYS
        = "CREATE TABLE keys ("
        + "id BINARY(20) PRIMARY KEY," // sha1(uri)
        + "bid BINARY(20) FOREIGN KEY REFERENCES buckets(id),"
        + "uri VARCHAR(16384) UNIQUE NOT NULL,"
        + "created DATETIME NOT NULL,"
        + "modified TIMESTAMP NOT NULL"
        + ")";
    
    private static final String CREATE_VALUES
        = "CREATE TABLE entries (" // Using 'entries' because 'values' is a reserved keyword
        + "id BINARY(20) PRIMARY KEY,"
        + "kid BINARY(20) FOREIGN KEY REFERENCES keys(id),"
        + "created DATETIME NOT NULL,"
        + "tombstone DATETIME"
        + ")";
    
    private static final String CREATE_VTAGS
        = "CREATE TABLE vtags ("
        + "id BIGINT PRIMARY KEY IDENTITY,"
        + "vtag BINARY(20) NOT NULL,"
        + "vid BINARY(20) NOT NULL FOREIGN KEY REFERENCES entries(id)"
        + ")";
    
    private static final String CREATE_PROPERTIES 
        = "CREATE TABLE properties ("
        + "id BIGINT PRIMARY KEY IDENTITY,"
        + "vid BINARY(20) FOREIGN KEY REFERENCES entries(id),"
        + "name VARCHAR(256) NOT NULL,"
        + "value VARCHAR(16384) NOT NULL,"
        + ")";
    
    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    public static DefaultIndex create(File dir) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            
            String url = "jdbc:hsqldb:mem:index-" + COUNTER.incrementAndGet();
            
            //String path = dir.getAbsolutePath() + "/index-" + COUNTER.incrementAndGet();
            //String url = "jdbc:hsqldb:file:" + path;
            
            String user = "sa";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);
            
            Statement statement = connection.createStatement();
            statement.addBatch(CREATE_BUCKETS);
            statement.addBatch(CREATE_KEYS);
            statement.addBatch(CREATE_VALUES);
            statement.addBatch(CREATE_VTAGS);
            statement.addBatch(CREATE_PROPERTIES);
            statement.executeBatch();
            statement.close();
            
            return new DefaultIndex(connection);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final Connection connection;
    
    private DefaultIndex(Connection connection) {
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
    
    @Override
    public List<String> listBuckets(String marker, int maxCount) throws SQLException {
        if (0 < maxCount) {
            PreparedStatement ps = null;
            
            try {
                if (marker != null) {
                    ps = connection.prepareStatement(
                            "SELECT name FROM buckets WHERE (name LIKE ?) ORDER BY(name) ASC LIMIT ?, ?");
                    ps.setString(1, marker + "%");
                    ps.setInt(2, 0);
                    ps.setInt(3, maxCount);
                } else {
                    ps = connection.prepareStatement(
                            "SELECT name FROM buckets ORDER BY(name) ASC LIMIT ?, ?");
                    ps.setInt(1, 0);
                    ps.setInt(2, maxCount);
                }
                
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        List<String> names = new ArrayList<String>();
                        
                        do {
                            String name = rs.getString(1);
                            names.add(name);
                        } while (names.size() < maxCount && rs.next());
                        
                        return names;
                    }
                } finally {
                    close(rs);
                }
            } finally {
                close(ps);
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<Key> listKeys(Key marker, int maxCount) throws SQLException {
        if (0 < maxCount) {
            
            PreparedStatement ps = null;
            try {
                if (marker != null) {
                    ps = connection.prepareStatement(
                            "SELECT uri FROM keys WHERE (bid = ? AND uri LIKE ?) ORDER BY(uri) ASC LIMIT ?, ?");
                
                    KUID bucketId = marker.getId();
                    URI uri = marker.getURI();
                    
                    setBytes(ps, 1, bucketId);
                    ps.setString(2, uri.toString() + "%");
                    ps.setInt(3, 0);
                    ps.setInt(4, maxCount);
                    
                } else {
                    ps = connection.prepareStatement(
                            "SELECT uri FROM keys ORDER BY(uri) ASC LIMIT ?, ?");
                    
                    ps.setInt(1, 0);
                    ps.setInt(2, maxCount);
                }
                
                ResultSet rs = ps.executeQuery();
                try {
                    
                    if (rs.next()) {
                        List<Key> keys = new ArrayList<Key>();
                        
                        do {
                            String key = rs.getString(1);
                            keys.add(DefaultKey.valueOf(key));
                        } while (keys.size() < maxCount && rs.next());
                        
                        return keys;
                    }
                    
                } finally {
                    close(rs);
                }
            } finally {
                close(ps);
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public List<KUID> listValues(Key key, KUID marker, int maxCount, Selector selector) throws SQLException {
        if (0 < maxCount) {
            
            StringBuilder query = new StringBuilder();
            query.append("SELECT id FROM entries WHERE (kid = ?");
            
            if (marker != null) {
                query.append(" AND id >= ?");
            }
            
            switch (selector) {
                case EVERYTHING:
                    break;
                case CURRENT:
                    query.append(" AND tombstone IS NULL");
                    break;
                case DELETED:
                    query.append(" AND tombstone IS NOT NULL");
                    break;
                default:
                    throw new IllegalArgumentException("selector=" + selector);
            }
            
            query.append(") ORDER BY(id) LIMIT ?, ?");
            
            KUID keyId = createKeyId(key);
            
            PreparedStatement ps = connection.prepareStatement(query.toString());
            try {
                if (marker != null) {
                    setBytes(ps, 1, keyId);
                    setBytes(ps, 2, marker);
                    
                    ps.setInt(3, 0);
                    ps.setInt(4, maxCount);
                    
                } else {
                    setBytes(ps, 1, keyId);
                    ps.setInt(2, 0);
                    ps.setInt(3, maxCount);
                }
                
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        List<KUID> values = new ArrayList<KUID>();
                        
                        do {
                            KUID valueId = KUID.create(rs.getBytes(1));
                            values.add(valueId);
                        } while (values.size() < maxCount && rs.next());
                        
                        return values;
                    }
                    
                } finally {
                    close(rs);
                }
            } finally {
                close(ps);
            }
        }
        
        return Collections.emptyList();
    }

    private int getKeyCount(KUID bucketId) throws SQLException {
        return count("SELECT COUNT(id) FROM keys WHERE bid = ?", bucketId);
    }

    @Override
    public int getValueCount(Key key) throws SQLException {
        KUID keyId = createKeyId(key);
        return getValueCount(keyId);
    }

    private int getValueCount(KUID keyId) throws SQLException {
        return count("SELECT COUNT(id) FROM entries WHERE kid = ?", keyId);
    }
    
    @Override
    public boolean containsKey(Key key) throws SQLException {
        KUID kid = createKeyId(key);
        return containsKey(kid);
    }
    
    private boolean containsKey(KUID keyId) throws SQLException {
        return 0 < count("SELECT COUNT(id) FROM keys WHERE id = ?", keyId);
    }
    
    @Override
    public boolean containsValue(KUID valueId) throws SQLException {
        return 0 < count("SELECT COUNT(id) FROM entries WHERE id = ?", valueId);
    }
    
    private boolean hasBucket(KUID bucketId) throws SQLException {
        return 0 < count("SELECT COUNT(id) FROM buckets WHERE id = ?", bucketId);
    }
    
    private int count(String sql, KUID key) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            setBytes(ps, 1, key);
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } finally {
                close(rs);
            }
        } finally {
            close(ps);
        }
        
        return 0;
    }
    
    @Override
    public List<Map.Entry<KUID, Context>> get(Key key, KUID marker, int maxCount, Selector selector) throws SQLException {
        
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT e.id, p.name, p.value FROM entries e, properties p")
            .append(" WHERE (e.kid = ? AND e.id = p.vid");
        
        if (marker != null) {
            query.append(" AND e.id >= ?");
        }
        
        switch (selector) {
            case EVERYTHING:
                break;
            case CURRENT:
                query.append(" AND e.tombstone IS NULL");
                break;
            case DELETED:
                query.append(" AND e.tombstone IS NOT NULL");
                break;
            default:
                throw new IllegalArgumentException("selector=" + selector);
        }
        
        query.append(") ORDER BY(e.created) DESC LIMIT ?, ?");
        
        KUID keyId = createKeyId(key);
        
        PreparedStatement ps = connection.prepareStatement(query.toString());
        try {
            
            if (marker != null) {
                setBytes(ps, 1, keyId);
                setBytes(ps, 2, marker);
                ps.setInt(3, 0);
                ps.setInt(4, maxCount);
            } else {
                setBytes(ps, 1, keyId);
                ps.setInt(2, 0);
                ps.setInt(3, maxCount);
            }
            
            ResultSet rs = ps.executeQuery();
            try {
                
                if (rs.next()) {
                    
                    List<Map.Entry<KUID, Context>> list 
                        = new ArrayList<Map.Entry<KUID,Context>>();
                    
                    Context context = null;
                    byte[] current = null;
                    
                    do {
                        byte[] valueId = rs.getBytes(1);
                        
                        if (!Arrays.equals(current, valueId)) {
                            context = new Context();
                            list.add(new DefaultEntry<KUID, Context>(
                                    KUID.create(valueId), context));
                        }
                        
                        String name = rs.getString(2);
                        String value = rs.getString(3);
                        context.addHeader(name, value);
                    } while (rs.next());
                    
                    return list;
                }
                
            } finally {
                close(rs);
            }
        } finally {
            close(ps);
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public void add(Key key, Context context, KUID valueId) throws SQLException {
        
        // Don't add if the Context doesn't have anything
        Header[] headers = context.getHeaders();
        if (headers == null || headers.length == 0) {
            return;
        }
        
        long now = System.currentTimeMillis();
        
        Date created = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        String bucket = key.getBucket();
        
        URI uri = key.getURI();
        KUID keyId = createKeyId(key);
        
        try {
            connection.setAutoCommit(false);
            
            // BUCKETS
            {
                PreparedStatement ps = null;
                try {
                    if (!hasBucket(bucketId)) {
                        ps = connection.prepareStatement(
                                "INSERT INTO buckets NAMES(id, name, created, modified) VALUES(?, ?, ?, ?)");
                        setBytes(ps, 1, bucketId);
                        ps.setString(2, bucket);
                        ps.setDate(3, created);
                        ps.setTimestamp(4, modified);
                    } else {
                        ps = connection.prepareStatement(
                                "UPDATE buckets SET modified = ? WHERE id = ?");
                        
                        ps.setTimestamp(1, modified);
                        setBytes(ps, 2, bucketId);
                    }
                    
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
            }
            
            // KEYS
            {
                PreparedStatement ps = null;
                try {
                    if (!containsKey(keyId)) {
                        ps = connection.prepareStatement(
                                "INSERT INTO keys NAMES(id, bid, uri, created, modified) VALUES(?, ?, ?, ?, ?)");
                        setBytes(ps, 1, keyId);
                        setBytes(ps, 2, bucketId);
                        ps.setString(3, uri.toString());
                        ps.setDate(4, created);
                        ps.setTimestamp(5, modified);
                    } else {
                        ps = connection.prepareStatement(
                                "UPDATE keys SET modified = ? WHERE id = ?");
                        
                        ps.setTimestamp(1, modified);
                        setBytes(ps, 2, keyId);
                    }
                    
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
            }
            
            // VALUES
            {
                PreparedStatement ps 
                    = connection.prepareStatement(
                        "INSERT INTO entries NAMES(id, kid, created) VALUES(?, ?, ?)");
                try {
                    setBytes(ps, 1, valueId);
                    setBytes(ps, 2, keyId);
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
                    for (Header header : headers) {
                        setBytes(ps, 1, valueId);
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
    
    @Override
    public void delete(Key key, Collection<KUID> values) throws SQLException {
        
        if (values != null && values.isEmpty()) {
            throw new IllegalArgumentException("values=" + values);
        }
        
        long now = System.currentTimeMillis();
        Date tombstone = new Date(now);
        
        KUID keyId = createKeyId(key);
        
        try {
            connection.setAutoCommit(false);
            
            PreparedStatement ps = null;
            try {
                if (values != null) {
                    
                    ps = connection.prepareStatement("UPDATE entries SET tombstone = ? WHERE id = ?"); 
                    
                    for (KUID valueId : values) {
                        
                        ps.setDate(1, tombstone);
                        setBytes(ps, 2, valueId);
                        
                        ps.addBatch();
                    }
                } else {
                    
                    ps = connection.prepareStatement("UPDATE entries SET tombstone = ? WHERE kid = ?");
                    
                    ps.setDate(1, tombstone);
                    setBytes(ps, 2, keyId);
                    ps.addBatch();
                }
                
                ps.executeBatch();
                
            } finally {
                close(ps);
            }
            
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    @Override
    public void remove(Key key, Collection<KUID> values) throws SQLException {
        
        if (values != null && values.isEmpty()) {
            throw new IllegalArgumentException("values=" + values);
        }
        
        long now = System.currentTimeMillis();
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        
        KUID keyId = createKeyId(key);
        
        try {
            connection.setAutoCommit(false);
            
            // PROPERTIES
            {
                PreparedStatement ps = null;
                try {
                    if (values != null) {
                        ps = connection.prepareStatement(
                                "DELETE FROM properties WHERE vid = ?");
                       for (KUID valueId : values) {
                           setBytes(ps, 1, valueId);
                           ps.addBatch();
                       }
                    } else {
                        ps = connection.prepareStatement(
                                "DELETE p FROM properties p INNER JOIN entries e ON p.vid = e.id WHERE e.kid = ?");
                        
                        setBytes(ps, 1, keyId);
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                } finally {
                    close(ps);
                }
            }
            
            // VALUES
            {
                PreparedStatement ps = null;
                try {
                    if (values != null) {
                        ps = connection.prepareStatement(
                            "DELETE FROM entries WHERE id = ?");
                        
                        for (KUID valueId : values) {
                            setBytes(ps, 1, valueId);
                            ps.addBatch();
                        }
                    } else {
                        ps = connection.prepareStatement(
                                "DELETE FROM entries WHERE kid = ?");
                        
                        setBytes(ps, 1, keyId);
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                    
                } finally {
                    close(ps);
                }
            }
            
            // KEYS
            {
                int count = getValueCount(keyId);
                PreparedStatement ps = null;
                try {
                    if (count < 1) {
                        ps = connection.prepareStatement(
                                "DELETE FROM keys WHERE id = ?");
                        setBytes(ps, 1, keyId);
                    } else {
                        ps = connection.prepareStatement(
                                "UPDATE keys SET modified = ? WHERE id = ?");
                        
                        ps.setTimestamp(1, modified);
                        setBytes(ps, 1, keyId);
                    }
                    
                    ps.executeUpdate();
                    
                } finally {
                    close(ps);
                }
            }
            
            // BUCKETS
            {
                int count = getKeyCount(bucketId);
                PreparedStatement ps = null;
                try {
                    if (count < 1) {
                        ps = connection.prepareStatement(
                                "DELETE FROM buckets WHERE id = ?");
                        setBytes(ps, 1, keyId);
                        
                    } else {
                        ps = connection.prepareStatement(
                                "UPDATE buckets SET modified = ? WHERE id = ?");
                        
                        ps.setTimestamp(1, modified);
                        setBytes(ps, 2, keyId);
                    }
                    
                    ps.executeUpdate();
                    
                } finally {
                    close(ps);
                }
            }
            
            connection.commit();
            
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static KUID createKeyId(Key key) {
        byte[] keyId = hash(key.getURI().toString());
        return KUID.create(keyId);
    }
    
    private static byte[] hash(String value) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        return md.digest(StringUtils.getBytes(value));
    }
    
    public static void main(String[] args) throws Exception {
        DefaultIndex index = DefaultIndex.create(null);
        
        /*Key key = KeyFactory.parseKey("ardverk:///hello/world");
        List<KUID> bla = new ArrayList<KUID>();
        
        for (int i = 0; i < 10; i++) {
            KUID valueId = KUID.createRandom(key.getId());
            
            Context context = new Context();
            context.addHeader("name1-" + i, "value1");
            context.addHeader("name2-" + i, "value2");
            
            index.add(key, context, valueId);
            
            System.out.println(index.get(valueId));
            bla.add(valueId);
        }
        
        System.out.println(Arrays.toString(index.get(key)));
        
        for (KUID valueId : bla) {
            index.remove(key, valueId);
            
            System.out.println("A: " + index.get(key).length);
        }*/
        
        /*for (int i = 0; i < 10; i++) {
            Key key = KeyFactory.parseKey("ardverk:///hello/world-" + i);
            KUID valueId = KUID.createRandom(key.getId());
            Context context = new Context();
            
            index.add(key, context, valueId);
        }
        
        Key prefix = KeyFactory.parseKey("ardverk:///hello/wor");
        List<Key> keys = index.listKeys(prefix, 5);
        
        System.out.println(keys.size());
        System.out.println(index.listBuckets(Integer.MAX_VALUE));*/
        
        Key key = KeyFactory.parseKey("ardverk:///hello/world");
        List<KUID> test = new ArrayList<KUID>();
        for (int i = 0; i < 10; i++) {Thread.sleep(500);
            KUID valueId = KUID.createRandom(key.getId());
            Context context = new Context();
            context.addHeader("X-Key", "" + i);
            
            index.add(key, context, valueId);
            
            test.add(valueId);
        }
        
        //Collections.sort(test);
        
        /*List<KUID> values = index.listValues(key, Integer.MAX_VALUE, null);
        
        System.out.println(values.size());
        System.out.println(values);
        System.out.println(values.containsAll(test));
        
        values = index.listValues(key, test.get(5), Integer.MAX_VALUE, null);
        
        System.out.println(values.size());
        System.out.println(values);
        System.out.println(values.containsAll(test));*/
        
        System.out.println(createKeyId(key));
        
        List<Map.Entry<KUID, Context>> values = index.get(key, 2, Selector.EVERYTHING);
        System.out.println(test);
        System.out.println(values);
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
    
    private static void setBytes(PreparedStatement ps, int index, KUID id) throws SQLException {
        ps.setBytes(index, id.getBytes(false));
    }
}
