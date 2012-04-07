package org.ardverk.dht.rsrc;

import java.nio.charset.Charset;

import org.ardverk.utils.StringUtils;

public class StringValue extends ByteArrayValue {

  public StringValue(String value) {
    this(StringUtils.getBytes(value));
  }
  
  public StringValue(String value, String encoding) {
    this(StringUtils.getBytes(value, encoding));
  }
  
  public StringValue(String value, Charset encoding) {
    this(StringUtils.getBytes(value, encoding));
  }
  
  public StringValue(byte[] value) {
    super(value);
  }
  
  public String toString(String encoding) {
    return StringUtils.toString(getContentAsBytes(), encoding);
  }
  
  public String toString(Charset encoding) {
    return StringUtils.toString(getContentAsBytes(), encoding);
  }
}
