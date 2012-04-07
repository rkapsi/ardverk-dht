package org.ardverk.dht.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.message.Context;
import org.ardverk.version.Occured;

class VclockMap {
  
  private final Map<KUID, Entry> map 
    = new LinkedHashMap<KUID, Entry>();
  
  public void upsert(Vclock vclock, Context context, Value value) {
    
    Iterator<Entry> it = map.values().iterator();
    while (it.hasNext()) {
      
      Vclock existing = it.next().vclock;
      
      Occured occured = VclockUtils.compare(existing, vclock);
      switch (occured) {
        case AFTER:
          it.remove();
          break;
        case BEFORE:
        case IDENTICAL:
          if (map.size() == 1) {
            return;
          }
          break;
      }
    }
    
    map.put(vclock.vtag(), new Entry(vclock, context, value));
  }
  
  public boolean remove(Vclock vclock) {
    return map.remove(vclock.vtag()) != null;
  }
  
  public Entry value(String vtag) {
    return map.get(vtag);
  }
  
  public Entry value() {
    return CollectionUtils.last(map.values());
  }
  
  public Entry[] values() {
    return CollectionUtils.toArray(map.values(), Entry.class);
  }
  
  public int size() {
    return map.size();
  }
  
  public boolean isEmpty() {
    return map.isEmpty();
  }
  
  public static class Entry {
    
    private final Vclock vclock;
    
    private final Context context;
    
    private final Value value;

    public Entry(Vclock vclock, Context context, Value value) {
      this.vclock = vclock;
      this.context = context;
      this.value = value;
    }

    public Vclock getVclock() {
      return vclock;
    }
    
    public Context getContext() {
      return context;
    }

    public Value getValue() {
      return value;
    }
  }
}
