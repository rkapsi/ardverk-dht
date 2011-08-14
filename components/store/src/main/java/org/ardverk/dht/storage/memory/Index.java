package org.ardverk.dht.storage.memory;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.message.Context;

public interface Index extends Closeable {

    public static enum Selector {
        EVERYTHING,
        CURRENT,
        DELETED;
    }
    
    public List<String> listBuckets(int maxCount) throws Exception;
    
    public List<String> listBuckets(String marker, int maxCount) throws Exception;
    
    public List<Key> listKeys(int maxCount) throws Exception;
    
    public List<Key> listKeys(Key marker, int maxCount) throws Exception;
    
    public List<KUID> listValues(Key key, int maxCount, Selector selector) throws Exception;
    
    public List<KUID> listValues(Key key, KUID marker, int maxCount, Selector selector) throws Exception;
    
    public boolean containsKey(Key key) throws Exception;

    public boolean containsValue(KUID valueId) throws Exception;

    public List<Map.Entry<KUID, Context>> get(Key key, int maxCount, Selector selector) throws Exception;
    
    public List<Map.Entry<KUID, Context>> get(Key key, KUID marker, int maxCount, Selector selector) throws Exception;
    
    public void add(Key key, Context context, KUID valueId) throws Exception;
    
    public void deleteAll(Key key) throws Exception;
    
    public void delete(Key key, Collection<KUID> values) throws Exception;
    
    public void removeAll(Key key) throws Exception;
    
    public void remove(Key key, Collection<KUID> values) throws Exception;
    
    public int getValueCount(Key key) throws Exception;
}