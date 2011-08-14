package org.ardverk.dht.storage.sql;

import static org.ardverk.dht.storage.sql.SQLUtils.setBytes;
import static org.ardverk.dht.storage.sql.StatementFactory.BUCKET_MODIFIED;
import static org.ardverk.dht.storage.sql.StatementFactory.DELETE_BUCKET;
import static org.ardverk.dht.storage.sql.StatementFactory.DELETE_KEY;
import static org.ardverk.dht.storage.sql.StatementFactory.DELETE_PROPERTIES;
import static org.ardverk.dht.storage.sql.StatementFactory.DELETE_VALUE;
import static org.ardverk.dht.storage.sql.StatementFactory.INSERT_PROPERTY;
import static org.ardverk.dht.storage.sql.StatementFactory.INSERT_VALUE;
import static org.ardverk.dht.storage.sql.StatementFactory.KEY_COUNT_BY_BUCKET_ID;
import static org.ardverk.dht.storage.sql.StatementFactory.KEY_MODIFIED;
import static org.ardverk.dht.storage.sql.StatementFactory.LIST_VALUE_ID;
import static org.ardverk.dht.storage.sql.StatementFactory.SET_TOMBSTONE;
import static org.ardverk.dht.storage.sql.StatementFactory.VALUE_COUNT_BY_KEY_ID;

import java.io.Closeable;
import java.io.File;
import java.net.URI;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.ardverk.collection.OrderedHashMap;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.DefaultKey;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Constants;
import org.ardverk.dht.storage.Context;
import org.ardverk.dht.storage.DateUtils;
import org.ardverk.dht.storage.sql.StatementFactory.Operation;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

public class DefaultIndex2 implements Closeable {
    
    public static final int LENGTH = 20;
    
    public static DefaultIndex2 create(File dir) {
        return create(dir, LENGTH);
    }
    
    public static DefaultIndex2 create(File dir, int length) {
        try {
            
            ConnectionManager cm = ConnectionManager.newInstance();
            StatementFactory factory = new StatementFactory(length);
            
            Statement statement = cm.createStatement();
            statement.addBatch(factory.createBuckets());
            statement.addBatch(factory.createKeys());
            statement.addBatch(factory.createValues());
            statement.addBatch(factory.createProperties());
            statement.executeBatch();
            statement.close();
            
            return new DefaultIndex2(factory, cm);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final StatementFactory factory;
    
    private final ConnectionManager cm;
    
    private DefaultIndex2(StatementFactory factory, ConnectionManager connection) {
        this.factory = factory;
        this.cm = connection;
    }
    
    @Override
    public void close() {
        cm.close();
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
            cm.beginTxn();
            
            // BUCKETS
            {
                PreparedStatement ps = cm.prepareStatement(
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
                PreparedStatement ps = cm.prepareStatement(
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
                PreparedStatement ps = cm.prepareStatement(INSERT_VALUE);
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
                PreparedStatement ps = cm.prepareStatement(INSERT_PROPERTY);
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
            
            cm.commit();
            
        } finally {
            cm.endTxn();
        }
    }
    
    private int getKeyCount(KUID bucketId) throws SQLException {
        return count(KEY_COUNT_BY_BUCKET_ID, bucketId);
    }
    
    private int getValueCount(KUID keyId) throws SQLException {
        return count(VALUE_COUNT_BY_KEY_ID, keyId);
    }
    
    private int count(String sql, KUID id) throws SQLException {
        PreparedStatement ps = cm.prepareStatement(sql);
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
        
        Keys keys = null;
        
        PreparedStatement ps = cm.prepareStatement(
                StatementFactory.listKeys(marker));
        
        try {
            
            int index = 0;
            if (marker != null) {
                ps.setString(++index, marker);
            }
            ps.setInt(++index, maxCount);
            
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    
                    keys = new Keys();
                    
                    do {
                        String uri = rs.getString(1);
                        
                        Key key = DefaultKey.valueOf(uri);
                        List<KUID> values = listValueIds(key);
                        
                        if (values != null) {
                            keys.put(key, values);
                        }
                        
                    } while (rs.next());
                }
            } finally {
                SQLUtils.close(rs);
            }
        } finally {
            SQLUtils.close(ps);
        }
        
        return keys;
    }
    
    private List<KUID> listValueIds(Key key) throws SQLException {
        
        KUID keyId = KeyId.valueOf(key);
        
        List<KUID> keys = null;
        
        PreparedStatement ps = cm.prepareStatement(LIST_VALUE_ID);
        try {
            
            setBytes(ps, 1, keyId);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                keys = new ArrayList<KUID>();
                
                do {
                    KUID valueId = KUID.create(rs.getBytes(1));
                    keys.add(valueId);
                } while (rs.next());
            }
        } finally {
            SQLUtils.close(ps);
        }
        
        return keys;
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
            cm.beginTxn();
            
            PreparedStatement ps = cm.prepareStatement(
                    StatementFactory.listValues(operation));
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
            cm.endTxn();
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
            cm.beginTxn();
            
            // VALUES (set tombstone)
            {
                boolean success = update(SET_TOMBSTONE, valueId, tombstone);
            }
            
            // KEYS (update modified)
            {
                boolean success = update(KEY_MODIFIED, keyId, modified);
            }
            
            // BUCKETS (update modified)
            {
                boolean success = update(BUCKET_MODIFIED, bucketId, modified);
            }
            
            cm.commit();
            
        } finally {
            cm.endTxn();
        }
    }
    
    public void remove(Key key, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        KUID keyId = KeyId.valueOf(key);
        
        try {
            cm.beginTxn();
            
            // PROPERTIES
            {
                boolean success = delete(DELETE_PROPERTIES, valueId);
            }
            
            // VALUES
            {
                boolean success = delete(DELETE_VALUE, valueId);
            }
            
            // KEYS
            {
                boolean success = false;
                int count = getValueCount(keyId);
                if (0 < count) {
                    success = update(KEY_MODIFIED, keyId, modified);
                } else {
                    success = delete(DELETE_KEY, keyId);
                }
            }
            
            // BUCKETS
            {
                boolean success = false;
                int count = getKeyCount(bucketId);
                if (0 < count) {
                    success = update(BUCKET_MODIFIED, bucketId, modified);
                } else {
                    success = delete(DELETE_BUCKET, bucketId);
                }
            }
            
            cm.commit();
            
        } finally {
            cm.endTxn();
        }
    }
    
    private boolean update(String sql, KUID id, java.util.Date date) throws SQLException {
        PreparedStatement ps = cm.prepareStatement(sql);
        try {
            
            if (date instanceof java.sql.Date) {
                ps.setDate(1, (java.sql.Date)date);
            } else if (date instanceof java.sql.Timestamp) {
                ps.setTimestamp(1, (java.sql.Timestamp)date);                
            } else {
                throw new IllegalArgumentException("date=" + date);
            }
            
            setBytes(ps, 2, id);
            return ps.executeUpdate() == 1;
        } finally {
            SQLUtils.close(ps);
        }
    }
    
    private boolean delete(String sql, KUID id) throws SQLException {
        PreparedStatement ps = cm.prepareStatement(sql);
        try {
            setBytes(ps, 1, id);
            return ps.executeUpdate() == 1;
        } finally {
            SQLUtils.close(ps);
        }
    }
    
    public static class Keys extends OrderedHashMap<Key, List<KUID>> {
        
        private static final long serialVersionUID = 6551875468885150502L;

        private Keys() {
            
        }
        
        private void add(Key key, KUID valueId) {
            List<KUID> list = get(key);
            if (list != null) {
                list = new ArrayList<KUID>();
                put(key, list);
            }
            list.add(valueId);
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
