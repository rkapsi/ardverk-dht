package org.ardverk.dht.storage.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.Value;


public class Request extends ContextMessage<Request> {

    private final Method method;
    
    public Request(Value value) {
        this(Method.PUT, value);
    }
    
    public Request(Method method, Value value) {
        super(value);
        
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
    protected void postCommitContext(OutputStream out) throws IOException {
        super.postCommitContext(out);
        method.writeTo(out);
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
