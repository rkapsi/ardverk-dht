package org.ardverk.dht.storage;

import java.io.Closeable;
import java.util.Map;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;

public interface Index extends Closeable {

    public boolean containsKey(Key key) throws Exception;

    public boolean containsValue(KUID valueId) throws Exception;

    public Map.Entry<KUID, Context>[] get(Key key) throws Exception;

    public Context get(KUID valueId) throws Exception;

    public void add(Key key, Context context, KUID valueId) throws Exception;
    
    public void remove(Key key, KUID valueId) throws Exception;
    
    public int getCount(Key key) throws Exception;
}