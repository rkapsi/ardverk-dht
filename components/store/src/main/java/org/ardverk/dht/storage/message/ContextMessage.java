package org.ardverk.dht.storage.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;

abstract class ContextMessage<T extends ContextMessage<?>> {

  protected final Context context;
  
  protected Value value = null;
  
  public ContextMessage() {
    this(new Context());
  }
  
  public ContextMessage(Value value) {
    this(new Context());
  }
  
  public ContextMessage(Context context) {
    this(context, null);
  }
  
  public ContextMessage(Context context, Value value) {
    this.context = context;
    this.value = value;
  }
  
  public Context getContext() {
    return context;
  }
  
  @SuppressWarnings("unchecked")
  protected final T self() {
    return (T)this;
  }
  
  public T setContentType(String contentType) {
    return setHeader(HTTP.CONTENT_TYPE, contentType);
  }
  
  public T setContentLength(long contentLength) {
    return setHeader(HTTP.CONTENT_LEN, Long.toString(contentLength));
  }
  
  public T setHeader(String name, String value) {
    context.addHeader(name, value);
    return self();
  }
  
  public T setValue(Value value) {
    this.value = value;
    return self();
  }
  
  public boolean containsHeader(String name) {
    return context.containsHeader(name);
  }
  
  public Value commit() throws IOException {
    
    preCommit();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      preCommitContext(baos);
      context.writeTo(baos);
      postCommitContext(baos);
    } finally {
      IoUtils.close(baos);
    }
    
    return new ContextValue(baos.toByteArray(), value);
  }
  
  protected void preCommit() {
    long contentLength = 0L;
    if (value != null) {
      if (!containsHeader(HTTP.CONTENT_LEN)) {
        contentLength = value.getContentLength();
        if (contentLength != 0L) {
          setContentLength(contentLength);
        }
      }
      
      if (!containsHeader(HTTP.CONTENT_TYPE)) {
        setContentType(HTTP.OCTET_STREAM_TYPE);
      }
    }
  }
  
  protected void preCommitContext(OutputStream out) throws IOException {
  }
  
  protected void postCommitContext(OutputStream out) throws IOException {
  }
  
  private static class ContextValue extends AbstractValue {
    
    private final byte[] context;
    
    private final Value value;
    
    public ContextValue(byte[] context, Value value) {
      this.context = context;
      this.value = value;
    }

    @Override
    public long getContentLength() {
      return context.length + (value != null ? value.getContentLength() : 0L);
    }

    @Override
    public InputStream getContent() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
      out.write(context);
      
      if (value != null) {
        value.writeTo(out);
      }
    }

    @Override
    public boolean isRepeatable() {
      return value == null || value.isRepeatable();
    }

    @Override
    public boolean isStreaming() {
      return value != null && value.isStreaming();
    }
  }
}
