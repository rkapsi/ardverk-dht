package org.ardverk.dht.storage.message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.storage.Constants;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.Writable;
import org.ardverk.utils.StringUtils;

public final class Context implements Properties, Writable, Cloneable {
    
    protected final HeaderGroup group;
    
    public Context() {
        this(new HeaderGroup());
    }
    
    public Context(HeaderGroup group) {
        this.group = group;
    }
    
    public Context(Context context) {
        this();
        
        addHeaders(context.getHeaders());
    }
    
    public String getStringValue(String name) {
        return getStringValue(name, null);
    }
    
    public String getStringValue(String name, String defaultValue) {
        Header header = getHeader(name);
        if (header != null) {
            return header.getValue();
        }
        return defaultValue;
    }
    
    public long getLongValue(String name) {
        return getLongValue(name, 0L);
    }
    
    public long getLongValue(String name, long defaultValue) {
        String value = getStringValue(name);
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }
    
    public long getContentLength() {
        return getLongValue(HTTP.CONTENT_LEN, -1L);
    }

    public String getETag() {
        return getStringValue(Constants.ETAG);
    }
    
    public String getContentMD5() {
        return getStringValue(Constants.CONTENT_MD5);
    }
    
    @Override
    public boolean containsHeader(String name) {
        return group.contains(name);
    }
    
    @Override
    public Header[] getHeaders() {
        return group.headers();
    }

    @Override
    public Header getHeader(String name) {
        return group.get(name);
    }

    @Override
    public Header addHeader(String name, String value) {
        Header header = new BasicHeader(name, value);
        addHeader(header);
        return header;
    }
    
    @Override
    public void addHeader(Header header) {
        group.add(header);
    }

    @Override
    public void addHeaders(Header... h) {
        group.clear();
        group.addAll(h);
    }

    @Override
    public Header removeHeader(String name) {
        return group.remove(name);
    }

    @Override
    public boolean removeHeader(Header header) {
        return group.remove(header);
    }
    
    @Override
    public void removeHeaders(Header... headers) {
        for (Header header : headers) {
            group.remove(header);
        }
    }
    
    @Override
    public Iterator<Header> iterator() {
        return group.iterator();
    }

    @Override
    public Context clone() {
        return new Context(this);
    }
    
    @Override
    public String toString() {
        return group.toString();
    }
    
    public static Context valueOf(File file) throws IOException {
        InputStream in = new BufferedInputStream(
                new FileInputStream(file));
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static Context valueOf(InputStream in) throws IOException {
        Context context = new Context();
        
        int count = DataUtils.beb2ushort(in);
        while (0 < count) {
            Header header = readHeader(in);
            context.addHeader(header);
            --count;
        }
        
        return context;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        Header[] headers = getHeaders();
        if (0xFFFF < headers.length) {
            throw new IOException();
        }
        
        DataUtils.short2beb(headers.length, out);
        for (Header header : headers) {
            writeHeader(header, out);
        }
    }
    
    private static void writeHeader(Header header, 
            OutputStream out) throws IOException {
        StringUtils.writeString(header.getName(), out);
        StringUtils.writeString(header.getValue(), out);
    }
    
    private static Header readHeader(InputStream in) throws IOException {
        String name = StringUtils.readString(in);
        String value = StringUtils.readString(in);
        return new BasicHeader(name, value);
    }
    
    /**
     * NOTE: {@link org.apache.http.message.HeaderGroup} has a JSR-133
     * related concurrency (initialization) issue. This class replicates 
     * its functionality, avoids the problem by declaring the list as 
     * {@code final} and speeds up some operations.
     */
    private static class HeaderGroup implements Iterable<Header> {
        
        private final Map<String, Header> map = Collections.synchronizedMap(
                new LinkedHashMap<String, Header>());
        
        private static String key(Header h) {
            return key(h.getName());
        }
        
        private static String key(String name) {
            return name.toLowerCase(Locale.US);
        }
        
        @Override
        public Iterator<Header> iterator() {
            return map.values().iterator();
        }
        
        public boolean contains(String name) {
            return map.containsKey(key(name));
        }
        
        public void add(Header header) {
            map.put(key(header), header);
        }
        
        public Header remove(String name) {
            return map.remove(key(name));
        }
        
        public boolean remove(Header header) {
            return map.values().remove(header);
        }
        
        public void addAll(Header... headers) {
            if (headers != null) {
                for (Header header : headers) {
                    add(header);
                }
            }
        }
        
        public Header get(String name) {
            return map.get(key(name));
        }
        
        public Header[] headers() {
            return map.values().toArray(new Header[0]);
        }
        
        public void clear() {
            map.clear();
        }
        
        @Override
        public String toString() {
            return map.values().toString();
        }
    }
}
