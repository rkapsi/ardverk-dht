package org.ardverk.dht.storage;


public class ContextUtils {

    private ContextUtils() {}
    
    public static long getLastModified(Context context) {
        return context.getLongValue(Constants.LAST_MODIFIED, -1L);
    }

    public static long getContentLength(Context context) {
        return context.getLongValue(Constants.LAST_MODIFIED, -1L);
    }

    public static String getETag(Context context) {
        return context.getStringValue(Constants.ETAG);
    }
}
