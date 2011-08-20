package org.ardverk.dht.rsrc;

import org.ardverk.utils.StringUtils;

public class StringValue extends ByteArrayValue {

    public StringValue(String value) {
        super(StringUtils.getBytes(value));
    }
}
