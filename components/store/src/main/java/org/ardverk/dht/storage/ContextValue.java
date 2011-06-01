package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.protocol.HTTP;

public class ContextValue extends ContextValueBase {
    
    protected volatile ValueEntity entity = null;
    
    public ContextValue() {
        super();
    }

    public ContextValue(Context context) {
        super(context);
    }
    
    public ContextValue(ValueEntity entity) {
        this(new Context(), entity);
    }
    
    public ContextValue(Context context, ValueEntity entity) {
        super(context);
        
        this.entity = entity;
    }

    public ValueEntity getEntity() {
        return entity;
    }

    public void setEntity(ValueEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public boolean isRepeatable() {
        ValueEntity entity = this.entity;
        return entity != null ? entity.isRepeatable() : true;
    }

    @Override
    public boolean isStreaming() {
        ValueEntity entity = this.entity;
        return entity != null ? entity.isStreaming() : false;
    }
    
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);
        
        ValueEntity entity = this.entity;
        if (entity != null) {
            entity.writeTo(out);
        }
    }

    @Override
    protected void writeContext(OutputStream out) throws IOException {
        ValueEntity entity = this.entity;
        if (entity != null) {
            setHeader(HTTP.CONTENT_TYPE, entity.getContentType());
            setHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength()));
        }
        
        super.writeContext(out);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        ValueEntity entity = this.entity;
        if (entity != null) {
            sb.append(entity).append(" ");
        }
        
        return sb.append(super.toString()).toString();
    }
}
