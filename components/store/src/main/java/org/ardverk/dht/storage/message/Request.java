package org.ardverk.dht.storage.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Request extends ContextValue {

    private final Method method;
    
    public Request(ValueEntity entity) {
        this(Method.PUT, entity);
    }
    
    public Request(Method method, ValueEntity entity) {
        super(entity);
        
        this.method = method;
    }
    
    private Request(Method method, Context context) {
        super(context);
        
        this.method = method;
    }
    
    public Method getMethod() {
        return method;
    }
    
    @Override
    protected void writeHeader(OutputStream out) throws IOException {
        method.writeTo(out);
        super.writeHeader(out);
    }
    
    @Override
    public String toString() {
        return method + " - " + super.toString();
    }
    
    public static Request valueOf(InputStream in) throws IOException {
        Method method = Method.valueOf(in);
        Context context = Context.valueOf(in);
        
        return new Request(method, context);
    }
}
