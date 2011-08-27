package org.ardverk.dht.storage.persistence;

import static org.ardverk.dht.storage.persistence.StatementFactory.BUCKET_MODIFIED;
import static org.ardverk.dht.storage.persistence.StatementFactory.DELETE_BUCKET;
import static org.ardverk.dht.storage.persistence.StatementFactory.DELETE_KEY;
import static org.ardverk.dht.storage.persistence.StatementFactory.DELETE_PROPERTIES;
import static org.ardverk.dht.storage.persistence.StatementFactory.DELETE_VALUE;
import static org.ardverk.dht.storage.persistence.StatementFactory.INSERT_PROPERTY;
import static org.ardverk.dht.storage.persistence.StatementFactory.INSERT_VALUE;
import static org.ardverk.dht.storage.persistence.StatementFactory.KEY_COUNT_BY_BUCKET_ID;
import static org.ardverk.dht.storage.persistence.StatementFactory.KEY_MODIFIED;
import static org.ardverk.dht.storage.persistence.StatementFactory.LIST_VALUE_ID;
import static org.ardverk.dht.storage.persistence.StatementFactory.SET_TOMBSTONE;
import static org.ardverk.dht.storage.persistence.StatementFactory.VALUE_COUNT_BY_KEY_ID;
import static org.ardverk.dht.storage.persistence.Utils.setBytes;

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

import org.apache.http.Header;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.DefaultKey;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Constants;
import org.ardverk.dht.storage.DateUtils;
import org.ardverk.dht.storage.message.Context;
import org.ardverk.io.IoUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;

class PersistedIndex implements Index {
    
    public static final int LENGTH = 20;
    
    public static PersistedIndex create(File dir) {
        return create(dir, LENGTH);
    }
    
    public static PersistedIndex create(File dir, int length) {
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
            
            return new PersistedIndex(factory, cm);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final StatementFactory factory;
    
    private final IConnectionManager cm;
    
    private PersistedIndex(StatementFactory factory, IConnectionManager connection) {
        this.factory = factory;
        this.cm = connection;
    }
    
    @Override
    public void close() {
        IoUtils.close(cm);
    }
    
    @Override
    public void add(Key key, Context context, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        Date created = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        context.addHeader(Constants.VALUE_ID, valueId.toHexString());
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
                    Utils.close(ps);
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
                    Utils.close(ps);
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
                    Utils.close(ps);
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
                    Utils.close(ps);
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
                Utils.close(rs);
            }
        } finally {
            Utils.close(ps);
        }
        
        return 0;
    }
    
    @Override
    public Keys keys(String marker, int maxCount) throws SQLException {
        
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
                Utils.close(rs);
            }
        } finally {
            Utils.close(ps);
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
            Utils.close(ps);
        }
        
        return keys;
    }
    
    @Override
    public Context get(Key key, KUID valueId) throws SQLException {
        Values values = values(key, valueId, 1);
        return values != null ? values.get(valueId) : null;
    }
    
    @Override
    public Values values(Key key, KUID marker, int maxCount) throws SQLException {
        Values values = null;
        
        KUID keyId = KeyId.valueOf(key);
        
        try {
            cm.beginTxn();
            
            PreparedStatement ps = cm.prepareStatement(
                    StatementFactory.listValues(marker, maxCount));
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
                    Utils.close(rs);
                }
                
            } finally {
                Utils.close(ps);
            }
            
        } finally {
            cm.endTxn();
        }
        
        return values;
    }
    
    @Override
    public boolean delete(Key key, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        
        Date tombstone = new Date(now);
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        KUID keyId = KeyId.valueOf(key);
        
        boolean success = true;
        
        try {
            cm.beginTxn();
            
            // VALUES (set tombstone)
            {
                success &= update(SET_TOMBSTONE, valueId, tombstone);
            }
            
            // KEYS (update modified)
            {
                success &= update(KEY_MODIFIED, keyId, modified);
            }
            
            // BUCKETS (update modified)
            {
                success &= update(BUCKET_MODIFIED, bucketId, modified);
            }
            
            cm.commit();
            
        } finally {
            cm.endTxn();
        }
        
        return success;
    }
    
    @Override
    public boolean remove(Key key, KUID valueId) throws SQLException {
        
        long now = System.currentTimeMillis();
        Timestamp modified = new Timestamp(now);
        
        KUID bucketId = key.getId();
        KUID keyId = KeyId.valueOf(key);
        
        boolean success = true;
        
        try {
            cm.beginTxn();
            
            // PROPERTIES
            {
                success &= delete(DELETE_PROPERTIES, valueId);
            }
            
            // VALUES
            {
                success &= delete(DELETE_VALUE, valueId);
            }
            
            // KEYS
            {
                int count = getValueCount(keyId);
                if (0 < count) {
                    success &= update(KEY_MODIFIED, keyId, modified);
                } else {
                    success &= delete(DELETE_KEY, keyId);
                }
            }
            
            // BUCKETS
            {
                int count = getKeyCount(bucketId);
                if (0 < count) {
                    success &= update(BUCKET_MODIFIED, bucketId, modified);
                } else {
                    success &= delete(DELETE_BUCKET, bucketId);
                }
            }
            
            cm.commit();
            
        } finally {
            cm.endTxn();
        }
        
        return success;
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
            Utils.close(ps);
        }
    }
    
    private boolean delete(String sql, KUID id) throws SQLException {
        PreparedStatement ps = cm.prepareStatement(sql);
        try {
            setBytes(ps, 1, id);
            return ps.executeUpdate() == 1;
        } finally {
            Utils.close(ps);
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
