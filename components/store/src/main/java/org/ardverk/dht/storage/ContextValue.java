package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.DefaultValue;

abstract class ContextValue extends DefaultValue implements Properties {

    protected final Context context;
    
    protected volatile ValueEntity value;
    
    public ContextValue() {
        this(new Context());
    }
    
    public ContextValue(Context context) {
        this(context, null);
    }
    
    public ContextValue(ValueEntity value) {
        this(new Context(), value);
    }
    
    public ContextValue(Context context, ValueEntity value) {
        this.context = context;
        this.value = value;
    }
    
    public Context getContext() {
        return context;
    }
    
    public ValueEntity getValueEntity() {
        return value;
    }

    public void setValueEntity(ValueEntity value) {
        this.value = value;
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
        ValueEntity value = this.value;
        return value != null ? value.isRepeatable() : true;
    }

    @Override
    public boolean isStreaming() {
        ValueEntity value = this.value;
        return value != null ? value.isStreaming() : false;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        writeContext(out);
        writeValue(out);
    }
    
    protected void writeContext(OutputStream out) throws IOException {
        
        ValueEntity value = this.value;
        if (value != null) {
            context.setHeader(HTTP.CONTENT_LEN, Long.toString(value.getContentLength()));
            context.setHeader(HTTP.CONTENT_TYPE, value.getContentType());
        } else {
            context.setHeader(Constants.NO_CONTENT);
        }
        
        context.writeTo(out);
    }
    
    protected void writeValue(OutputStream out) throws IOException {
        ValueEntity value = this.value;
        if (value != null) {
            value.writeTo(out);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        ValueEntity value = this.value;
        if (value != null) {
            sb.append(value).append(" ");
        }
        
        sb.append(context);
        
        return sb.toString();
    }
}
