package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Value;

public class ContextValue extends AbstractValue implements Properties {
    
    protected final Context context;
    
    protected final Value value;
    
    public ContextValue() {
        this(new Context(), null);
    }
    
    public ContextValue(Value value) {
        this(new Context(), value);
    }
    
    public ContextValue(Context context) {
        this(context, null);
    }
    
    public ContextValue(Context context, Value value) {
        this.context = context;
        this.value = value;
    }
    
    public Context getContext() {
        return context;
    }
    
    public Value getValue() {
        return value;
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
    public boolean isRepeatable() {
        return value != null ? value.isRepeatable() : true;
    }

    @Override
    public boolean isStreaming() {
        return value != null ? value.isStreaming() : false;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        context.writeTo(out);
        
        if (value != null) {
            value.writeTo(out);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            sb.append(value).append(" ");
        }
        sb.append(context);
        return sb.toString();
    }
}
