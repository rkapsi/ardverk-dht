package org.ardverk.dht.storage;

import java.util.NoSuchElementException;

import org.apache.http.Header;
import org.apache.http.protocol.HTTP;

public class ContextUtils {

    private ContextUtils() {}
    
    public static String getStringValue(Context context, String name) {
        Header header = context.getFirstHeader(name);
        if (header != null) {
            return header.getValue();
        }
        throw new NoSuchElementException(name);
    }
    
    public static long getLongValue(Context context, String name) {
        return Long.parseLong(getStringValue(context, name));
    }
    
    public static long getLastModified(Context context) {
        return getLongValue(context, Constants.LAST_MODIFIED);
    }

    public static long getContentLength(Context context) {
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            return getLongValue(context, HTTP.CONTENT_LEN);
        }
        return -1L;
    }

    public static String getETag(Context context) {
        return getStringValue(context, Constants.ETAG);
    }
}
