package org.ardverk.dht.storage.message;

import org.apache.http.Header;

public interface Properties extends Iterable<Header> {

  public boolean containsHeader(String name);

  public Header[] getHeaders();

  public Header getHeader(String name);

  public Header addHeader(String name, String value);

  public void addHeader(Header header);

  public void addHeaders(Header... h);

  public Header removeHeader(String name);

  public boolean removeHeader(Header header);

  public void removeHeaders(Header... headers);
}