package org.ardverk.dht.config2;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.Duration;
import org.ardverk.dht.concurrent.ExecutorKey;

public class Config implements Serializable, Cloneable {
    
    private static final long serialVersionUID = -5787512165124341866L;

    private static enum Category {
        GENERAL,
        PING,
        LOOKUP,
        FIND_NODE,
        FIND_VALUE,
        STORE,
        PUT, // FIND_NODE + STORE
        BOOTSTRAP,
        QUICKEN,
        ;
    }
    
    public static enum Key {
        
        // GENERAL
        K(Category.GENERAL, int.class, 20),
        EXECUTOR_KEY(Category.GENERAL, ExecutorKey.class, ExecutorKey.DEFAULT),
        OPERATION_TIMEOUT(Category.GENERAL, 1L, TimeUnit.MINUTES),
        RTTM(Category.GENERAL, double.class, -1),
        
        // PING
        // --- None ---
        
        // LOOKUP
        EXHAUSTIVE(Category.LOOKUP, boolean.class, false),
        RANDOMIZE(Category.LOOKUP, boolean.class, false),
        ALPHA(Category.LOOKUP, int.class, 4),
        BOOST_FREQUENCY(Category.LOOKUP, 5L, TimeUnit.SECONDS),
        BOOST_TIMEOUT(Category.LOOKUP, 3L, TimeUnit.SECONDS),
        LOOKUP_TIMEOUT(Category.LOOKUP, 10L, TimeUnit.SECONDS),
        
        // FIND_NODE
        
        // FIND_VALUE
        R(Category.FIND_VALUE, int.class, 1),
        
        // STORE
        STORE_TIMEOUT(Category.STORE, 10L, TimeUnit.SECONDS),
        S(Category.STORE, int.class, 5),
        W(Category.STORE, int.class, Key.K),
        
        // BOOTSTRAP
        
        // PUT
        
        // QUICKEN
        PING_COUNT(Category.QUICKEN, float.class, 1.0f),
        CONTACT_TIMEOUT(Category.QUICKEN, 1L, TimeUnit.MINUTES),
        BUCKET_TIMEOUT(Category.QUICKEN, 1L, TimeUnit.MINUTES),
        
        ;
        
        private final Category category;
        
        private final Class<?> type;
        
        private final Object defaultValue;
        
        private Key(Category category, long duration, TimeUnit unit) {
            this(category, Duration.class, new Duration(duration, unit));
        }
        
        private Key(Category category, Class<?> type, Object defaultValue) {
            this.category = category;
            this.type = type;
            this.defaultValue = defaultValue;
        }
        
        private Object check(Object value) {
            if (!type.isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("value=" + value);
            }
            
            return value;
        }
        
        private <T> T cast(Object value, Class<T> clazz) {
            if (value != null) {
                if (!type.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("value=" + value);
                }
                
                return clazz.cast(value);
            }
            
            return clazz.cast(defaultValue);
        }
        
        public boolean isAssignable(Object value) {
            Class<?> clazz = value.getClass();
            return type.isAssignableFrom(clazz);
        }
        
        @Override
        public String toString() {
            return name() + "(" + category + ", " + type.getName() 
                    + ", " + defaultValue + ")";
        }
    }
    
    private final Map<Key, Object> map;
    
    public Config() {
        this(new ConcurrentHashMap<Key, Object>());
    }
    
    private Config(Map<Key, Object> map) {
        this.map = map;
    }
    
    public Config putTime(Key key, long time, TimeUnit unit) {
        return putObject(key, new Duration(time, unit));
    }
    
    public long getTime(Key key, TimeUnit unit) {
        return getObject(key, Duration.class).getTime(unit);
    }
    
    public Config putBoolean(Key key, boolean value) {
        return putObject(key, value);
    }
    
    public boolean getBoolean(Key key) {
        return getObject(key, Boolean.class).booleanValue();
    }
    
    public Config putLong(Key key, long value) {
        return putObject(key, value);
    }
    
    public long getLong(Key key) {
        return getObject(key, Number.class).longValue();
    }
    
    public Config putDouble(Key key, double value) {
        return putObject(key, value);
    }
    
    public double getDouble(Key key) {
        return getObject(key, Number.class).doubleValue();
    }
    
    public Object getObject(Key key) {
        return getObject(key, Object.class);
    }
    
    public <T> T getObject(Key key, Class<T> type) {
        Object value = map.get(key);
        if (value instanceof Key) {
            return getObject((Key)value, type);
        }
        
        return key.cast(value, type);
    }
    
    public Config putObject(Key key, Object value) {
        map.put(key, key.check(value));
        return this;
    }
    
    @Override
    public Config clone() {
        return new Config(new ConcurrentHashMap<Key, Object>(map));
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
}
