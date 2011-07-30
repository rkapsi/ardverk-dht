package org.ardverk.dht.storage.sql;

import static org.ardverk.dht.storage.sql.SQLUtils.beginTxn;
import static org.ardverk.dht.storage.sql.SQLUtils.endTxn;
import static org.ardverk.dht.storage.sql.SQLUtils.setBytes;

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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.ardverk.collection.OrderedHashMap;
import org.ardverk.collection.OrderedMap;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Constants;
import org.ardverk.dht.storage.Context;
import org.ardverk.dht.storage.Vclock;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

public class DefaultIndex2 implements Closeable {
    
    public static final int LENGTH = 20;
    
    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    public static DefaultIndex2 create(File dir) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            
            String url = "jdbc:hsqldb:mem:index-" + COUNTER.incrementAndGet();
            
            //String path = dir.getAbsolutePath() + "/index-" + COUNTER.incrementAndGet();
            //String url = "jdbc:hsqldb:file:" + path;
            
            String user = "sa";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);
            
            StatementFactory factory = new StatementFactory(LENGTH);
            
            Statement statement = connection.createStatement();
            statement.addBatch(factory.createBuckets());
            statement.addBatch(factory.createKeys());
            statement.addBatch(factory.createValues());
            statement.addBatch(factory.createVTags());
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
    
    public void add(Key key, Vclock vclock, 
            Context context, KUID valueId) throws SQLException {
        
        context.addHeader(Constants.VCLOCK, vclock.toString());
        context.addHeader(Constants.VTAG, vclock.vtag64());
        
        KUID bucketId = key.getId();
        String bucket = key.getBucket();
        
        URI uri = key.getURI();
        KUID keyId = createKeyId(key);
        
        KUID vtag = vclock.vtag();
        
        long now = System.currentTimeMillis();
        Date created = new Date(now);
        Timestamp modified = new Timestamp(now);
        
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
                PreparedStatement ps = connection.prepareStatement(
                        StatementFactory.INSERT_VALUES);
                try {
                    setBytes(ps, 1, valueId);
                    setBytes(ps, 2, keyId);
                    ps.setDate(3, created);
                    ps.executeUpdate();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            // VTAGS
            {
                PreparedStatement ps = connection.prepareStatement(
                        StatementFactory.INSERT_VTAGS);
                try {
                    setBytes(ps, 1, vtag);
                    setBytes(ps, 2, valueId);
                    ps.executeUpdate();
                } finally {
                    SQLUtils.close(ps);
                }
            }
            
            // PROPERTIES
            {
                PreparedStatement ps = connection.prepareStatement(
                        StatementFactory.INSERT_PROPERTIES);
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
    
    public Values getValues(Key key, KUID marker, int maxCount) throws SQLException {
        Values values = null;
        
        KUID keyId = createKeyId(key);
        
        PreparedStatement ps = connection.prepareStatement(
                StatementFactory.getValues(marker));
        try {
            
            int index = 0;
            
            setBytes(ps, ++index, keyId);
            if (marker != null) {
                setBytes(ps, ++index, marker);
            }
            //ps.setInt(++index, 0);
            ps.setInt(++index, maxCount);
            
            ResultSet rs = ps.executeQuery();
            try {
                
                if (rs.next()) {
                    
                    int count = rs.getInt(4);
                    values = new Values(count);
                    
                    byte[] currentId = null;
                    Context context = null;
                    
                    do {
                        byte[] valueId = rs.getBytes(1);
                        
                        if (!Arrays.equals(currentId, valueId)) {
                            context = values.next(valueId);
                            currentId = valueId;
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
        
        return values;
    }
    
    private static KUID createKeyId(Key key) {
        byte[] keyId = hash(key.getURI().toString());
        return KUID.create(keyId);
    }
    
    private static byte[] hash(String value) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        return md.digest(StringUtils.getBytes(value));
    }
    
    public static class Values {
        
        private final OrderedMap<KUID, Context> values 
            = new OrderedHashMap<KUID, Context>();
        
        private final int count;
        
        private Values(int count) {
            this.count = count;
        }
        
        private Context next(byte[] valueId) {
            Context context = new Context();
            values.put(KUID.create(valueId), context);
            return context;
        }
        
        public int getCount() {
            return count;
        }
        
        public OrderedMap<KUID, Context> values() {
            return values;
        }
        
        @Override
        public String toString() {
            return "count=" + count + ", values=" + values;
        }
    }
}
