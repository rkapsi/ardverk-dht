package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;

public class Request extends ContextValue {

    private final Method method = Method.PUT;
    
    public Request(ValueEntity entity) {
        super(entity);
    }
    
    @Override
    protected void writeContext(OutputStream out) throws IOException {
        method.writeTo(out);
        super.writeContext(out);
    }
}
