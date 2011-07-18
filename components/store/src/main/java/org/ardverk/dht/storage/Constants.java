package org.ardverk.dht.storage;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

public class Constants {
    
    public static final String CLIENT_ID = "X-Ardverk-ClientId";
    
    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VCLOCK = "X-Ardverk-Vclock";

    public static final String VTAG = "X-Ardverk-Vtag";
    
    public static final String VALUE_ID = "X-Ardverk-ValueId";
    
    public static final String CONTENT_MD5 = "Content-MD5";
    
    public static final String CONTENT_SHA1 = "Content-SHA1";
    
    public static final String XML_TEXT_TYPE = "text/xml";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String ETAG = "ETag";
    
    public static final String TOMBSTONE = "Tombstone";
    
    public static final Header NO_CONTENT = new BasicHeader(HTTP.CONTENT_LEN, "0");

    public static final Header SERVER = new BasicHeader(HTTP.SERVER_HEADER, "Ardverk-DHT/0.1");
    
    public static final Header XML = new BasicHeader(HTTP.CONTENT_TYPE, XML_TEXT_TYPE);
    
    private Constants() {}
    
    public static Header date() {
        return new BasicHeader(HTTP.DATE_HEADER, DateUtils.now());
    }
    
    public static Header tombstone() {
        return new BasicHeader(TOMBSTONE, DateUtils.now());
    }
    
    public static void init(Properties properties) {
        properties.addHeader(date());
        properties.addHeader(SERVER);
    }
}
