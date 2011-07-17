package org.ardverk.dht.storage;

import java.util.List;

abstract class AbstractIndex implements Index {
    
    @Override
    public List<String> listBuckets(int maxCount) throws Exception {
        return listBuckets(null, maxCount);
    }
}
