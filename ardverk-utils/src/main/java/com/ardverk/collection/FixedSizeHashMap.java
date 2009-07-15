package com.ardverk.collection;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link Map} with a fixed-size capacity.
 */
public class FixedSizeHashMap<K, V> extends LinkedHashMap<K, V> 
        implements FixedSize, Serializable {
    
    private static final long serialVersionUID = -8289709441678695668L;
    
    protected final int maxSize;

    public FixedSizeHashMap(int maxSize) {
        super();
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeHashMap(int initialCapacity, float loadFactor, 
            boolean accessOrder, int maxSize) {
        super(initialCapacity, loadFactor, accessOrder);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeHashMap(int initialCapacity, float loadFactor, int maxSize) {
        super(initialCapacity, loadFactor);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeHashMap(int initialCapacity, int maxSize) {
        super(initialCapacity);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeHashMap(Map<? extends K, ? extends V> m, int maxSize) {
        super(m);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }
    
    @Override
    public boolean isFull() {
        return size() >= getMaxSize();
    }
    
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if (size() > getMaxSize()) {
            removing(eldest);
            return true;
        }
        return false;
    }
    
    /**
     * 
     */
    protected void removing(Map.Entry<K, V> eldest) {
        // OVERRIDE
    }
}
