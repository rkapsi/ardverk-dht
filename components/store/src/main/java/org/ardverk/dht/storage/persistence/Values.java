package org.ardverk.dht.storage.persistence;

import java.util.Map;

import org.ardverk.collection.OrderedHashMap;
import org.ardverk.dht.KUID;
import org.ardverk.dht.storage.message.Context;

public class Values extends OrderedHashMap<KUID, Context> {
    
    private static final long serialVersionUID = -1211452362899524359L;

    private final KUID marker;
    
    private final int count;
    
    public Values(KUID marker, int count) {
        this.marker = marker;
        this.count = count;
    }
    
    public KUID getMarker() {
        return marker;
    }
    
    public int getCount() {
        return count;
    }
    
    public Context getOrCreate(byte[] valueId, int maxCount) {
        return getOrCreateContext(KUID.create(valueId), maxCount);
    }
    
    private Context getOrCreateContext(KUID valueId, int maxCount) {
        Context context = get(valueId);
        if (context == null) {
            assert (size() < maxCount) : "Check the SQL query!";
            
            context = new Context();
            put(valueId, context);
        }
        return context;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("count=").append(count)
            .append(", size=").append(size())
            .append(", values: {\n");
        
        for (Map.Entry<KUID, Context> entry : entrySet()) {
            sb.append(" ").append(entry).append("\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}