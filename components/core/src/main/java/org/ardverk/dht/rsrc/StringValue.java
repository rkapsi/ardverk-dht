package org.ardverk.dht.rsrc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;

public class StringValue extends ByteArrayValue {

    public StringValue(String value) {
        super(StringUtils.getBytes(value));
    }
    
    public static StringValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static StringValue valueOf(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4*1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } finally {
            IoUtils.close(baos);
        }
        return new StringValue(StringUtils.toString(baos.toByteArray()));
    }
}
