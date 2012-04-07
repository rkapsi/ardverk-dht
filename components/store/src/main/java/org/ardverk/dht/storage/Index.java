package org.ardverk.dht.storage;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.ardverk.collection.OrderedHashMap;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.message.Context;

/**
 * 
 */
public interface Index extends Closeable {

  /**
   * 
   */
  public void add(Key key, Context context, KUID valueId) throws Exception;

  /**
   * 
   */
  public Keys keys(String marker, int maxCount) throws Exception;

  /**
   * 
   */
  public Context get(Key key, KUID valueId) throws Exception;

  /**
   * 
   */
  public Values values(Key key, KUID marker, int maxCount) throws Exception;

  /**
   * 
   */
  public boolean delete(Key key, KUID valueId) throws Exception;

  /**
   * 
   */
  public boolean remove(Key key, KUID valueId) throws Exception;

  /**
   * 
   */
  public static class Keys extends OrderedHashMap<Key, List<KUID>> {
    
    private static final long serialVersionUID = 6551875468885150502L;
    
    public Keys() {}
  }
  
  /**
   * 
   */
  public static class Values extends OrderedHashMap<KUID, Context> {
    
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
        assert (size() < maxCount);
        
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
}