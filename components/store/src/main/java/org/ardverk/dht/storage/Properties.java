package org.ardverk.dht.storage;

import java.util.Iterator;

import org.apache.http.Header;

public interface Properties extends Iterable<Header> {

    public abstract boolean containsHeader(String name);

    public abstract void addHeader(String name, String value);

    public abstract void addHeader(Header header);

    public abstract Header[] getHeaders();

    public abstract Header getFirstHeader(String name);

    public abstract Header[] getHeaders(String name);

    public abstract Header getLastHeader(String name);

    public abstract Header setHeader(String name, String value);

    public abstract void setHeader(Header header);

    public abstract void setHeaders(Header... h);

    public abstract Header[] removeHeaders(String name);

    public abstract void removeHeader(Header header);

    public abstract void removeHeaders(Header... headers);

    public abstract Iterator<Header> iterator(String name);
}