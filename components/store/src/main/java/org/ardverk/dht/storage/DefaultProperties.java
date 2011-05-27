package org.ardverk.dht.storage;

import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

public class DefaultProperties implements Properties {

    protected final HeaderGroup group;
    
    public DefaultProperties() {
        this(new HeaderGroup());
    }
    
    public DefaultProperties(HeaderGroup group) {
        this.group = group;
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
    public String toString() {
        return group.toString();
    }
}
