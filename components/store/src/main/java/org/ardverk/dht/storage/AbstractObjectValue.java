package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.rsrc.AbstractValue;

public abstract class AbstractObjectValue extends AbstractValue implements ObjectValue {

    protected final HeaderGroup headers;
    
    public AbstractObjectValue() {
        this(new HeaderGroup());
    }
    
    public AbstractObjectValue(HeaderGroup headers) {
        this.headers = headers;
    }
    
    @Override
    public boolean containsHeader(String name) {
        return headers.containsHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void addHeader(Header header) {
        headers.addHeader(header);
    }

    @Override
    public Header[] getHeaders() {
        return headers.getAllHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        return headers.getFirstHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return headers.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return headers.getLastHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(Header header) {
        headers.updateHeader(header);
    }

    @Override
    public void setHeaders(Header... h) {
        headers.setHeaders(h);
    }

    @Override
    public void removeHeaders(String name) {
        for (Header header : getHeaders(name)) {
            removeHeader(header);
        }
    }

    @Override
    public void removeHeader(Header header) {
        headers.removeHeader(header);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Header> headerIterator() {
        return headers.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Header> headerIterator(String name) {
        return headers.iterator(name);
    }
    
    @Override
    public abstract void writeTo(OutputStream out) throws IOException;
    
    @Override
    public String toString() {
        return headers.toString();
    }
}
