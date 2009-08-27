package com.ardverk.dht.message;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ardverk.utils.StringUtils;

public class BencodingOutputStream extends FilterOutputStream {

    private static final Integer TRUE = Integer.valueOf(1);
    
    private static final Integer FALSE = Integer.valueOf(0);
    
    private final String encoding;
    
    public BencodingOutputStream(OutputStream out) {
        this(out, StringUtils.UTF_8);
    }
    
    public BencodingOutputStream(OutputStream out, String encoding) {
        super(out);
        
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        
        this.encoding = encoding;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    //@SuppressWarnings("unchecked")
    public void writeObject(Object obj) throws IOException {
        if (obj instanceof byte[]) {
            byte[] bytes = (byte[])obj;
            byte[] length = Integer.toString(bytes.length)
                                .getBytes(encoding);
            
            write(length);
            write(':');
            write(bytes);
            
        } else if (obj instanceof Boolean) {
            if (((Boolean)obj).booleanValue()) {
                writeObject(TRUE);
            } else {
                writeObject(FALSE);
            }
            
        } else if (obj instanceof Number) {
            String num = ((Number)obj).toString();
            write('i');
            write(num.getBytes(encoding));
            write('e');
            
        } else if (obj instanceof String) {
            writeObject(((String)obj).getBytes(encoding));
        
        } else if (obj instanceof List<?>) {
            List<?> list = (List<?>)obj;
            write('l');
            for (Object o : list) {
                writeObject(o);
            }
            write('e');
        
        } else if (obj instanceof Map<?, ?>) {
            Map<String, ?> map = (Map<String, ?>)obj;
            if (!(map instanceof SortedMap<?, ?>)) {
                map = new TreeMap<String, Object>(map);
            }
            
            write('d');
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                
                if (!(key instanceof String)) {
                    throw new IOException("Key must be a String: " + key);
                }
                
                writeObject(key);
                writeObject(value);
            }
            write('e');
        } else if (obj instanceof Enum<?>) {
            writeObject(((Enum<?>)obj).name());
            
        } else {
            throw new IOException("Cannot bencode " + obj);
        }
    }
}
