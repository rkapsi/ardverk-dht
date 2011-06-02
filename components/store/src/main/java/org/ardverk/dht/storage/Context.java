package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
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
    
    @Override
    public boolean containsHeader(String name) {
        return group.containsHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void addHeader(Header header) {
        group.addHeader(header);
    }

    @Override
    public Header[] getHeaders() {
        return group.getAllHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        return group.getFirstHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return group.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return group.getLastHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        setHeader(new BasicHeader(name, value));
    }
    
    @Override
    public void setHeader(Header header) {
        group.updateHeader(header);
    }

    @Override
    public void setHeaders(Header... h) {
        group.setHeaders(h);
    }

    @Override
    public Header[] removeHeaders(String name) {
        Header[] headers = getHeaders(name);
        removeHeaders(headers);
        return headers;
    }

    @Override
    public void removeHeader(Header header) {
        group.removeHeader(header);
    }
    
    @Override
    public void removeHeaders(Header... headers) {
        for (Header header : headers) {
            group.removeHeader(header);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Header> iterator() {
        return group.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
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
}
