package org.ardverk.dht.storage.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;

public class Response extends ContextMessage<Response> {

  private final StatusLine statusLine;
  
  public Response(StatusLine statusLine) {
    this(statusLine, new Context());
  }
  
  public Response(StatusLine statusLine, Context context) {
    this(statusLine, context, null);
  }
  
  public Response(StatusLine statusLine, 
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
  
  @Override
  public String toString() {
    return statusLine + " - " + super.toString();
  }
  
  public static Response valueOf(Value value) throws IOException {
    InputStream in = value.getContent();
    try {
      return valueOf(in);
    } finally {
      IoUtils.close(in);
    }
  }
  
  public static Response valueOf(InputStream in) throws IOException {
    StatusLine statusLine = StatusLine.valueOf(in);
    Context context = Context.valueOf(in);
    
    long contentLength = context.getContentLength();
    byte[] content = new byte[(int)contentLength];
    StreamUtils.readFully(in, content);
    
    return new Response(statusLine, context, new ByteArrayValue(content));
  }
}
