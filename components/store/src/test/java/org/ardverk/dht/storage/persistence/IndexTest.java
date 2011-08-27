package org.ardverk.dht.storage.persistence;

import java.util.Set;
import java.util.TreeSet;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyFactory;
import org.ardverk.dht.storage.Index;
import org.ardverk.dht.storage.message.Context;
import org.ardverk.dht.storage.persistence.PersistedIndex.Values;
import org.junit.Test;

public class IndexTest {

    @Test
    public void add() throws Exception {
        Index index = PersistedIndex.create(null);
        
        Set<KUID> k = new TreeSet<KUID>();
        Key key = KeyFactory.parseKey("ardverk:///hello/world");
        for (int i = 0; i < 10; i++) {
            KUID valueId = KUID.createRandom(key.getId());
            Context context = new Context();
            context.addHeader("X-Key", "" + i);
            
            index.add(key, context, valueId);
            k.add(valueId);
            //Thread.sleep(1000);
        }
        
        for (KUID kk : k) {
            System.out.println(kk);
        }
        
        KUID marker = CollectionUtils.nth(k, k.size()/2-1);
        Values values = index.values(key, marker, 6);
        System.out.println(values);
    }
}
