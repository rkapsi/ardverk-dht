package org.ardverk.dht.storage;

import java.util.List;
import java.util.Map;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;

public abstract class AbstractIndex implements Index {
    
    @Override
    public List<String> listBuckets(int maxCount) throws Exception {
        return listBuckets(null, maxCount);
    }
    
    @Override
    public List<Key> listKeys(int maxCount) throws Exception {
        return listKeys(null, maxCount);
    }
    
    @Override
    public List<KUID> listValues(Key key, int maxCount, Selector selector) throws Exception {
        return listValues(key, null, maxCount, selector);
    }
    
    @Override
    public List<Map.Entry<KUID, Context>> get(Key key, int maxCount, Selector selector) throws Exception {
        return get(key, null, maxCount, selector);
    }
    
    @Override
    public void deleteAll(Key key) throws Exception {
        delete(key, null);
    }
    
    public void removeAll(Key key) throws Exception {
        remove(key, null);
    }
}
