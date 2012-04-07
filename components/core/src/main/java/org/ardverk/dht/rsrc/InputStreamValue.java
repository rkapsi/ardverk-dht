package org.ardverk.dht.rsrc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamValue extends AbstractValue implements Closeable {
  
  private final long length;
  
  private final InputStream in;
  
  public InputStreamValue(long length, InputStream in) {
    this.length = length;
    this.in = in;
  }
  
  @Override
  public long getContentLength() {
    return length;
  }

  @Override
  public InputStream getContent() {
    return in;
  }
  
  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public boolean isStreaming() {
    return true;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
