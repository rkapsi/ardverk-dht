package org.ardverk.dht.storage;

import java.util.Iterator;

import org.apache.http.Header;
import org.ardverk.dht.rsrc.Value;

public interface ObjectValue extends Value {

    public boolean containsHeader(String name);
    
    public void addHeader(String name, String value);
    
    public void addHeader(Header header);
    
    public Header[] getHeaders();
    
    public Header getFirstHeader(String name);
    
    public Header[] getHeaders(String name);
    
    public Header getLastHeader(String name);
    
    public void setHeader(String name, String value);
    
    public void setHeader(Header header);
    
    public void setHeaders(Header... headers);
    
    public void removeHeaders(String name);
    
    public void removeHeader(Header header);
    
    public Iterator<Header> headerIterator();
    
    public Iterator<Header> headerIterator(String name);
}
