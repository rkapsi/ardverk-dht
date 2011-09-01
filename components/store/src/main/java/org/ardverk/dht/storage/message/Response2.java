package org.ardverk.dht.storage.message;

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.Value;

public class Response2 extends ContextMessage<Response2> {

    private final StatusLine statusLine;
    
    public Response2(StatusLine statusLine) {
        this(statusLine, new Context());
    }
    
    public Response2(StatusLine statusLine, Context context) {
        this(statusLine, context, null);
    }
    
    public Response2(StatusLine statusLine, 
            Context context, Value value) {
        super(context, value);
        this.statusLine = statusLine;
    }
    
    public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override
    protected void preCommitContext(OutputStream out) throws IOException {
        super.preCommitContext(out);
        statusLine.writeTo(out);
    }
}
