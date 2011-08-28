package org.ardverk.dht.storage.sql;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyFactory;
import org.ardverk.dht.storage.Index;
import org.ardverk.dht.storage.Index.Values;
import org.ardverk.dht.storage.message.Context;
import org.junit.Test;

public class DefaultIndexTest {

    @Test
    public void add() throws Exception {
        
        final int count = 10;
        
        File dir = new File("data/test");
        Index index = DefaultIndex.create(dir);
        
        Set<KUID> dst = new TreeSet<KUID>();
        Key key = KeyFactory.parseKey("ardverk:///hello/world");
        for (int i = 0; i < count; i++) {
            KUID valueId = KUID.createRandom(key.getId());
            Context context = new Context();
            context.addHeader("X-Index", Integer.toString(i));
            
            index.add(key, context, valueId);
            dst.add(valueId);
        }
        
        final int m = dst.size()/2-1;
        final int maxCount = 6;
        
        KUID marker = CollectionUtils.nth(dst, m);
        Values values = index.values(key, marker, maxCount);
        
        TestCase.assertEquals(marker, values.firstKey());
        TestCase.assertTrue(values.size() <= maxCount);
    }
}
