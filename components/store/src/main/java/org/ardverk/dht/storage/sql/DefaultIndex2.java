package org.ardverk.dht.storage.sql;

import static org.ardverk.dht.storage.sql.SQLUtils.beginTxn;
import static org.ardverk.dht.storage.sql.SQLUtils.endTxn;
import static org.ardverk.dht.storage.sql.SQLUtils.setBytes;
import static org.ardverk.dht.storage.sql.StatementFactory.BUCKET_MODIFIED;
import static org.ardverk.dht.storage.sql.StatementFactory.INSERT_PROPERTY;
import static org.ardverk.dht.storage.sql.StatementFactory.INSERT_VALUE;
import static org.ardverk.dht.storage.sql.StatementFactory.KEY_MODIFIED;
import static org.ardverk.dht.storage.sql.StatementFactory.SET_TOMBSTONE;
import static org.ardverk.dht.storage.sql.StatementFactory.VALUE_COUNT_BY_KEY_ID;

import java.io.Closeable;
import java.io.File;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.ardverk.collection.OrderedHashMap;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Constants;
import org.ardverk.dht.storage.Context;
import org.ardverk.dht.storage.DateUtils;
import org.ardverk.dht.storage.sql.StatementFactory.Operation;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

public class DefaultIndex2 implements Closeable {
    
    public static final int LENGTH = 20;
    
    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    public static DefaultIndex2 create(File dir) {
        return create(dir, LENGTH);
    }
    
    public static DefaultIndex2 create(File dir, int length) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            
            String url = "jdbc:hsqldb:mem:index-" + COUNTER.incrementAndGet();
            
            //String path = dir.getAbsolutePath() + "/index-" + COUNTER.incrementAndGet();
            //String url = "jdbc:hsqldb:file:" + path;
            
            String user = "sa";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);
            
            StatementFactory factory = new StatementFactory(length);
            
            Statement statement = connection.createStatement();
            statement.addBatch(factory.createBuckets());
            statement.addBatch(factory.createKeys());
            statement.addBatch(factory.createValues());
            statement.addBatch(factory.createProperties());
            statement.executeBatch();
            statement.close();
            
            return new DefaultIndex2(factory, connection);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final StatementFactory factory;
    
    private final Connection connection;
    
    private DefaultIndex2(StatementFactory factory, Connection connection) {
        this.factory = factory;
        this.connection = connection;
    }
    
    @Override
    public void close() {
        SQLUtils.close(connection);
    }
    
    public void add(Key key, Context context, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        Date created = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        context.addHeader(Constants.VALUE_ID, valueId.toHexString());
        //context.addHeader(Constants.VCLOCK, vclock.toString());
        //context.addHeader(Constants.VTAG, vclock.vtag64());
        context.addHeader(Constants.DATE, DateUtils.format(now));
        
        KUID bucketId = key.getId();
        String bucket = key.getBucket();
        
        URI uri = key.getURI();
        KUID keyId = KeyId.valueOf(key);
        
        try {
            beginTxn(connection);
            
            // BUCKETS
            {
                PreparedStatement ps = connection.prepareStatement(
                        factory.upsertBuckets());
                try {
                    setBytes(ps, 1, bucketId);
                    ps.setString(2, bucket);
                    ps.setDate(3, created);
                    ps.setTimestamp(4, modified);
                    ps.executeUpdate();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            // KEYS
            {
                PreparedStatement ps = connection.prepareStatement(
                        factory.upsertKeys());
                try {
                    setBytes(ps, 1, keyId);
                    setBytes(ps, 2, bucketId);
                    ps.setString(3, uri.toString());
                    ps.setDate(4, created);
                    ps.setTimestamp(5, modified);
                    ps.executeUpdate();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            // VALUES
            {
                PreparedStatement ps = connection.prepareStatement(INSERT_VALUE);
                try {
                    setBytes(ps, 1, valueId);
                    setBytes(ps, 2, keyId);
                    ps.setDate(3, created);
                    ps.executeUpdate();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            // PROPERTIES
            {
                PreparedStatement ps = connection.prepareStatement(INSERT_PROPERTY);
                try {
                    
                    for (Header header : context.getHeaders()) {
                        String name = header.getName();
                        String value = header.getValue();
                        
                        setBytes(ps, 1, valueId);
                        ps.setString(2, name);
                        ps.setString(3, value);
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            connection.commit();
            
        } finally {
            endTxn(connection);
        }
    }
    
    private int getValueCount(KUID keyId) throws SQLException {
        return count(VALUE_COUNT_BY_KEY_ID, keyId);
    }
    
    private int count(String sql, KUID id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            setBytes(ps, 1, id);
            
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } finally {
                SQLUtils.close(rs);
            }
        } finally {
            SQLUtils.close(ps);
        }
        
        return 0;
    }
    
    public Keys listKeys(String marker, int maxCount) throws SQLException {
        
        return null;
    }
    
    public Context get(Key key, KUID valueId) throws SQLException {
        Values values = listValues(key, valueId, 1);
        return values != null ? values.get(valueId) : null;
    }
    
    public Values listValues(Key key, KUID marker, int maxCount) throws SQLException {
        Values values = null;
        
        KUID keyId = KeyId.valueOf(key);
        Operation operation = Operation.valueOf(marker, maxCount);
        
        try {
            beginTxn(connection);
            
            PreparedStatement ps = connection.prepareStatement(
                    StatementFactory.getValues(operation));
            try {
                
                int index = 0;
                
                setBytes(ps, ++index, keyId);
                if (marker != null) {
                    setBytes(ps, ++index, marker);
                }
                ps.setInt(++index, maxCount);
                
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        
                        int count = getValueCount(keyId);
                        values = new Values(marker, count);
                        
                        do {
                            byte[] valueId = rs.getBytes(1);
                            Context context = values.getOrCreate(valueId, maxCount);
                            
                            if (context == null) {
                                break;
                            }
                            
                            String name = rs.getString(2);
                            String value = rs.getString(3);
                            
                            context.addHeader(name, value);
                            
                        } while (rs.next());
                    }
                    
                } finally {
                    SQLUtils.close(rs);
                }
                
            } finally {
                SQLUtils.close(ps);
            }
            
        } finally {
            endTxn(connection);
        }
        
        return values;
    }
    
    public void delete(Key key, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        
        Date tombstone = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        KUID keyId = KeyId.valueOf(key);
        
        try {
            beginTxn(connection);
            
            // VALUES (set tombstone)
            {
                int success = updateDate(SET_TOMBSTONE, valueId, tombstone);
            }
            
            // KEYS (update modified)
            {
                int success = updateDate(KEY_MODIFIED, keyId, modified);
            }
            
            // BUCKETS (update modified)
            {
                int success = updateDate(BUCKET_MODIFIED, bucketId, modified);
            }
            
            connection.commit();
            
        } finally {
            endTxn(connection);
        }
    }
    
    private int updateDate(String sql, KUID id, java.util.Date date) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        try {
            
            if (date instanceof java.sql.Date) {
                ps.setDate(1, (java.sql.Date)date);
            } else if (date instanceof java.sql.Timestamp) {
                ps.setTimestamp(1, (java.sql.Timestamp)date);                
            } else {
                throw new IllegalArgumentException("date=" + date);
            }
            
            setBytes(ps, 2, id);
            return ps.executeUpdate();
        } finally {
            SQLUtils.close(ps);
        }
    }
    
    public static class Keys {
        
        private Keys() {
            
        }
    }
    
    public static class Values extends OrderedHashMap<KUID, Context> {
        
        private static final long serialVersionUID = -1211452362899524359L;

        private final KUID marker;
        
        private final int count;
        
        private Values(KUID marker, int count) {
            this.marker = marker;
            this.count = count;
        }
        
        public KUID getMarker() {
            return marker;
        }
        
        public int getCount() {
            return count;
        }
        
        private Context getOrCreate(byte[] valueId, int maxCount) {
            return getOrCreateContext(KUID.create(valueId), maxCount);
        }
        
        private Context getOrCreateContext(KUID valueId, int maxCount) {
            Context context = get(valueId);
            if (context == null) {
                assert (size() < maxCount) : "Check the SQL query!";
                
                context = new Context();
                put(valueId, context);
            }
            return context;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("count=").append(count)
                .append(", size=").append(size())
                .append(", values: {\n");
            
            for (Map.Entry<KUID, Context> entry : entrySet()) {
                sb.append(" ").append(entry).append("\n");
            }
            
            sb.append("}");
            return sb.toString();
        }
    }
    
    private static class KeyId {
        
        private KeyId() {}
        
        public static KUID valueOf(Key key) {
            byte[] keyId = hash(key.getURI().toString());
            return KUID.create(keyId);
        }
        
        private static byte[] hash(String value) {
            MessageDigest md = MessageDigestUtils.createSHA1();
            return md.digest(StringUtils.getBytes(value));
        }
    }
}
