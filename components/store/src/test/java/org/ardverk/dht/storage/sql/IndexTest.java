package org.ardverk.dht.storage.sql;

import java.sql.SQLException;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.KeyFactory;
import org.ardverk.dht.storage.Context;
import org.ardverk.dht.storage.Vclock;
import org.ardverk.dht.storage.sql.DefaultIndex2.Values;
import org.junit.Test;

public class IndexTest {

    @Test
    public void add() throws SQLException {
        DefaultIndex2 index = DefaultIndex2.create(null);
        
        Key key = KeyFactory.parseKey("ardverk:///hello/world");
        for (int i = 0; i < 10; i++) {
            KUID valueId = KUID.createRandom(key.getId());
            Context context = new Context();
            context.addHeader("X-Key", "" + i);
            
            Vclock vclock = Vclock.create(key);
            
            index.add(key, vclock, context, valueId);
        }
        
        Values values = index.getValues(key, null, 5);
        System.out.println(values);
    }
}
