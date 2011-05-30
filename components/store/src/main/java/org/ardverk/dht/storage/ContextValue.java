package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.Value;

public class ContextValue extends AbstractContextValue {
    
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
        super(context);
        
        assert (!(value instanceof ContextValue));
        this.value = value;
    }

    public Value getValue() {
        return value;
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
    protected void writeValue(OutputStream out) throws IOException {
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
        
        return sb.append(super.toString()).toString();
    }
}
