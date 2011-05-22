package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Value;

public class ContextValue extends AbstractValue {

    private final Context context;
    
    private final Value value;
    
    public ContextValue(Context context, Value value) {
        this.context = context;
        this.value = value;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        context.writeTo(out);
        value.writeTo(out);
    }
}
