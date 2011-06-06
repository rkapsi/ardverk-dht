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
    public Header[] getHeaders() {
        return context.getHeaders();
    }

    @Override
    public Header getHeader(String name) {
        return context.getHeader(name);
    }
    
    @Override
    public Header addHeader(String name, String value) {
        return context.addHeader(name, value);
    }

    @Override
    public void addHeader(Header header) {
        context.addHeader(header);
    }

    @Override
    public void addHeaders(Header... h) {
        context.addHeaders(h);
    }

    @Override
    public Header removeHeader(String name) {
        return context.removeHeader(name);
    }

    @Override
    public boolean removeHeader(Header header) {
        return context.removeHeader(header);
    }

    @Override
    public void removeHeaders(Header... headers) {
        context.removeHeaders(headers);
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
        writeHeader(out);
        writeValue(out);
    }
    
    protected void writeHeader(OutputStream out) throws IOException {
        
        ValueEntity value = this.value;
        if (value != null) {
            context.addHeader(HTTP.CONTENT_LEN, Long.toString(value.getContentLength()));
            context.addHeader(HTTP.CONTENT_TYPE, value.getContentType());
        } else {
            context.addHeader(Constants.NO_CONTENT);
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
