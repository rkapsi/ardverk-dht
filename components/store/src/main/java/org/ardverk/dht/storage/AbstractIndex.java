package org.ardverk.dht.storage;

import java.util.List;
import java.util.Map;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;

abstract class AbstractIndex implements Index {
    
    @Override
    public List<String> listBuckets(int maxCount) throws Exception {
        return listBuckets(null, maxCount);
    }
    
    @Override
    public Map<KUID, Context> delete(Key key) throws Exception {
        return delete(key, null);
    }
}
