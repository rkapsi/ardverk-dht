package org.ardverk.dht.rsrc;

import org.ardverk.utils.StringUtils;

public class StringValue extends ByteArrayValue {

    public StringValue(String value) {
        this(StringUtils.getBytes(value));
    }
    
    public StringValue(byte[] value) {
        super(value);
    }
}
