package org.ardverk.dht.storage;

import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

public class Properties implements Iterable<Header> {

    protected final HeaderGroup group;
    
    public Properties() {
        this(new HeaderGroup());
    }
    
    public Properties(HeaderGroup group) {
        this.group = group;
    }
    
    public boolean containsHeader(String name) {
        return group.containsHeader(name);
    }

    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

    public void addHeader(Header header) {
        group.addHeader(header);
    }

    public Header[] getHeaders() {
        return group.getAllHeaders();
    }

    public Header getFirstHeader(String name) {
        return group.getFirstHeader(name);
    }

    public Header[] getHeaders(String name) {
        return group.getHeaders(name);
    }

    public Header getLastHeader(String name) {
        return group.getLastHeader(name);
    }

    public void setHeader(String name, String value) {
        setHeader(new BasicHeader(name, value));
    }

    public void setHeader(Header header) {
        group.updateHeader(header);
    }

    public void setHeaders(Header... h) {
        group.setHeaders(h);
    }

    public void removeHeaders(String name) {
        for (Header header : getHeaders(name)) {
            removeHeader(header);
        }
    }

    public void removeHeader(Header header) {
        group.removeHeader(header);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Header> iterator() {
        return group.iterator();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Header> iterator(String name) {
        return group.iterator(name);
    }
    
    @Override
    public String toString() {
        return group.toString();
    }
}
