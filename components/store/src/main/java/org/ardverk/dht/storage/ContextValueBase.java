package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.ardverk.dht.rsrc.DefaultValue;

class ContextValueBase extends DefaultValue implements Properties {

    protected final Context context;
    
    public ContextValueBase() {
        this(new Context());
    }
    
    public ContextValueBase(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    
    @Override
    public Iterator<Header> iterator() {
        return context.iterator();
    }

    @Override
    public boolean containsHeader(String name) {
        return context.containsHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        context.addHeader(name, value);
    }

    @Override
    public void addHeader(Header header) {
        context.addHeader(header);
    }

    @Override
    public Header[] getHeaders() {
        return context.getHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        return context.getFirstHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return context.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return context.getLastHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        context.setHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        context.setHeader(header);
    }

    @Override
    public void setHeaders(Header... h) {
        context.setHeaders(h);
    }

    @Override
    public Header[] removeHeaders(String name) {
        return context.removeHeaders(name);
    }

    @Override
    public void removeHeader(Header header) {
        context.removeHeader(header);
    }

    @Override
    public void removeHeaders(Header... headers) {
        context.removeHeaders(headers);
    }

    @Override
    public Iterator<Header> iterator(String name) {
        return context.iterator(name);
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        writeContext(out);
    }
    
    protected void writeContext(OutputStream out) throws IOException {
        context.writeTo(out);
    }
    
    @Override
    public String toString() {
        return context.toString();
    }
}
