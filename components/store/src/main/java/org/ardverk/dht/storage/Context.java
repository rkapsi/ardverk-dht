package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.HTTP;
import org.ardverk.io.DataUtils;
import org.ardverk.io.Writable;
import org.ardverk.utils.StringUtils;

public final class Context extends ObjectProperties 
        implements Cloneable, Writable {

    public Context() {
        super();
    }

    public Context(HeaderGroup group) {
        super(group);
    }
    
    public Context(Context context) {
        super(context);
    }
    
    public String getStringValue(String name) {
        Header header = getFirstHeader(name);
        if (header != null) {
            return header.getValue();
        }
        throw new NoSuchElementException(name);
    }
    
    public long getLongValue(String name) {
        return Long.parseLong(getStringValue(name));
    }
    
    public long getLastModified() {
        return getLongValue(Constants.LAST_MODIFIED);
    }

    public long getContentLength() {
        if (containsHeader(HTTP.CONTENT_LEN)) {
            return getLongValue(HTTP.CONTENT_LEN);
        }
        return -1L;
    }

    public String getETag() {
        return getStringValue(Constants.ETAG);
    }
    
    @Override 
    public Context clone() {
        return new Context(this);
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
}
