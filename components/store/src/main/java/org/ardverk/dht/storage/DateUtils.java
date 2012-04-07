package org.ardverk.dht.storage;

import org.apache.commons.lang.time.FastDateFormat;

public class DateUtils {

  public static final String RFC1123_PATTERN 
    = "EEE, dd MMM yyyy HH:mm:ss zzz";
    
  public static final FastDateFormat RFC1123 
    = FastDateFormat.getInstance(RFC1123_PATTERN);

  private DateUtils() {}
  
  /**
   * Returns the current date and time in RFC 1123 format.
   */
  public static String now() {
    return format(System.currentTimeMillis());
  }
  
  /**
   * Returns the given date and time in RFC 1123 format.
   */
  public static String format(long time) {
    return RFC1123.format(time);
  }
}
