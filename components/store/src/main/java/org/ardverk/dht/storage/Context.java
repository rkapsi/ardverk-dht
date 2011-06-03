package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.ardverk.io.DataUtils;
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
        
        setHeaders(context.getHeaders());
    }
    
    public String getStringValue(String name) {
        return getStringValue(name, null);
    }
    
    public String getStringValue(String name, String defaultValue) {
        Header header = getFirstHeader(name);
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

    public String getETag(Context context) {
        return getStringValue(Constants.ETAG);
    }
    
    @Override
    public boolean containsHeader(String name) {
        return group.contains(name);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void addHeader(Header header) {
        group.add(header);
    }

    @Override
    public Header[] getHeaders() {
        return group.headers();
    }

    @Override
    public Header getFirstHeader(String name) {
        return group.first(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return group.headers(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return group.last(name);
    }

    @Override
    public Header setHeader(String name, String value) {
        Header header = new BasicHeader(name, value);
        setHeader(header);
        return header;
    }
    
    @Override
    public void setHeader(Header header) {
        group.replace(header);
    }

    @Override
    public void setHeaders(Header... h) {
        group.clear();
        group.addAll(h);
    }

    @Override
    public Header[] removeHeaders(String name) {
        Header[] headers = getHeaders(name);
        removeHeaders(headers);
        return headers;
    }

    @Override
    public void removeHeader(Header header) {
        group.remove(header);
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
    public Iterator<Header> iterator(String name) {
        return group.iterator(name);
    }
    
    @Override
    public Context clone() {
        return new Context(this);
    }
    
    @Override
    public String toString() {
        return group.toString();
    }
    
    public static Context valueOf(InputStream in) throws IOException {
        Context context = new Context();
        
        int count = DataUtils.beb2ushort(in);
        for (int i = 0; i < count; i++) {
            Header header = readHeader(in);
            context.addHeader(header);
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
        
        private final List<Header> list = new ArrayList<Header>();
        
        @Override
        public Iterator<Header> iterator() {
            return list.iterator();
        }
        
        public Iterator<Header> iterator(String name) {
            return Arrays.asList(headers(name)).iterator();
        }
        
        public boolean contains(String name) {
            return first(name) != null;
        }
        
        public void add(Header header) {
            if (header != null) {
                list.add(header);
            }
        }
        
        public void remove(Header header) {
            if (header != null) {
                list.remove(header);
            }
        }
        
        public void addAll(Header... headers) {
            if (headers != null) {
                for (Header header : headers) {
                    add(header);
                }
            }
        }
        
        public Header first(String name) {
            name = name.toLowerCase();
            
            for (Header header : list) {
                if (equalsIgnoreCase(name, header)) {
                    return header;
                }
            }
            return null;
        }
        
        public Header last(String name) {
            name = name.toLowerCase();
            
            for (int i = list.size()-1; i >= 0; --i) {
                Header header = list.get(i);
                if (equalsIgnoreCase(name, header)) {
                    return header;
                }
            }
            return null;
        }
        
        public Header[] headers() {
            return list.toArray(new Header[0]);
        }
        
        public Header[] headers(String name) {
            List<Header> dst = new ArrayList<Header>();
            
            name = name.toLowerCase();
            for (Header header : list) {
                if (equalsIgnoreCase(name, header)) {
                    dst.add(header);
                }
            }
            
            return dst.toArray(new Header[0]);
        }
        
        public void replace(Header header) {
            String name = header.getName().toLowerCase();
            
            int size = list.size();
            for (int i = 0; i < size; i++) {
                Header other = list.get(i);
                if (equalsIgnoreCase(name, other)) {
                    list.set(i, header);
                    return;
                }
            }
            
            add(header);
        }
        
        public void clear() {
            list.clear();
        }
        
        @Override
        public String toString() {
            return list.toString();
        }
        
        /**
         * NOTE: It's being assumed that the {@link String} argument
         * is in lower case (see {@link String#toLowerCase()}).
         */
        private static boolean equalsIgnoreCase(String name, Header header) {
            return name.equals(header.getName().toLowerCase());
        }
    }
}
