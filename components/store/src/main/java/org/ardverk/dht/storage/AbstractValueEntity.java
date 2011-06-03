package org.ardverk.dht.storage;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.DefaultValue;

abstract class AbstractValueEntity extends DefaultValue implements ValueEntity {
    
    private final String contentType;
    
    private final long contentLength;
    
    public AbstractValueEntity(Context context) {
        this(context.getStringValue(HTTP.CONTENT_TYPE),
                context.getLongValue(HTTP.CONTENT_LEN));
    }
    
    public AbstractValueEntity(String contentType, long contentLength) {
        this.contentType = contentType;
        this.contentLength = contentLength;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public long getContentLength() {
        return contentLength;
    }
}
