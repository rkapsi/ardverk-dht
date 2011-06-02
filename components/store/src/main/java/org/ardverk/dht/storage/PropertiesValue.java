package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.ardverk.dht.rsrc.DefaultValue;
import org.ardverk.io.Writable;

class PropertiesValue<T extends Properties & Writable> 
        extends DefaultValue implements Properties {

    protected final T properties;
    
    public PropertiesValue(T properties) {
        this.properties = properties;
    }

    public T getProperties() {
        return properties;
    }
    
    @Override
    public Iterator<Header> iterator() {
        return properties.iterator();
    }

    @Override
    public boolean containsHeader(String name) {
        return properties.containsHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        properties.addHeader(name, value);
    }

    @Override
    public void addHeader(Header header) {
        properties.addHeader(header);
    }

    @Override
    public Header[] getHeaders() {
        return properties.getHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        return properties.getFirstHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return properties.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return properties.getLastHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        properties.setHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        properties.setHeader(header);
    }

    @Override
    public void setHeaders(Header... h) {
        properties.setHeaders(h);
    }

    @Override
    public Header[] removeHeaders(String name) {
        return properties.removeHeaders(name);
    }

    @Override
    public void removeHeader(Header header) {
        properties.removeHeader(header);
    }

    @Override
    public void removeHeaders(Header... headers) {
        properties.removeHeaders(headers);
    }

    @Override
    public Iterator<Header> iterator(String name) {
        return properties.iterator(name);
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        writeContext(out);
    }
    
    protected void writeContext(OutputStream out) throws IOException {
        properties.writeTo(out);
    }
    
    @Override
    public String toString() {
        return properties.toString();
    }
}
