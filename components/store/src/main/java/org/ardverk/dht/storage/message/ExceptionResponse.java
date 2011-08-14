package org.ardverk.dht.storage.message;

import org.apache.http.protocol.HTTP;
import org.ardverk.lang.ExceptionUtils;
import org.ardverk.utils.StringUtils;

public class ExceptionResponse extends Response {

    public static Response create(Throwable t) {
        return create(StatusLine.INTERNAL_SERVER_ERROR, t);
    }
    
    public static Response create(StatusLine status, Throwable t) {
        String value = ExceptionUtils.toString(t);
        ValueEntity entity = new ByteArrayValueEntity(
                HTTP.PLAIN_TEXT_TYPE, 
                StringUtils.getBytes(value));
        
        return new ExceptionResponse(status, entity);
    }
    
    private ExceptionResponse(StatusLine status, ValueEntity entity) {
        super(status, entity);
    }
}
