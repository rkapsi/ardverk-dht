package org.ardverk.dht.storage;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

class Constants {

    public static final String CLIENT_ID = "X-Ardverk-ClientId";
    
    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VCLOCK = "X-Ardverk-Vclock";

    public static final String VTAG = "X-Ardverk-Vtag";
    
    public static final String CONTENT_MD5 = "Content-MD5";
    
    public static final String XML_TEXT_TYPE = "text/xml";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String ETAG = "ETag";
    
    public static final Header NO_CONTENT = new BasicHeader(HTTP.CONTENT_LEN, "0");

    private Constants() {}
}
