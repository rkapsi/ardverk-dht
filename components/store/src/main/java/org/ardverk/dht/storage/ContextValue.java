package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Value;

public class ContextValue extends AbstractValue {
    
    private final Context context;
    
    private final Value value;
    
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
