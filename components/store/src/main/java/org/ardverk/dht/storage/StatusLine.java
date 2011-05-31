package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpStatus;
import org.ardverk.io.DataUtils;
import org.ardverk.io.Writable;
import org.ardverk.utils.StringUtils;

public class StatusLine implements Writable {

    public static final StatusLine OK = new StatusLine(
            HttpStatus.SC_OK, "OK");
    
    public static final StatusLine MULTIPLE_CHOICES = new StatusLine(
            HttpStatus.SC_MULTIPLE_CHOICES, "Multiple Choices");
    
    public static final StatusLine NOT_FOUND = new StatusLine(
            HttpStatus.SC_NOT_FOUND, "Not Found");
    
    public static final StatusLine INTERNAL_SERVER_ERROR = new StatusLine(
            HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    
    public static final StatusLine LENGTH_REQUIRED = new StatusLine(
            HttpStatus.SC_LENGTH_REQUIRED, "Length Required");
    
    private final int code;
    
    private final String message;
    
    public StatusLine(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        DataUtils.short2beb(code, out);
        StringUtils.writeString(message, out);
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof StatusLine)) {
            return false;
        }
        
        StatusLine other = (StatusLine)o;
        return code == other.code && message.equals(other.message);
    }

    @Override
    public String toString() {
        return code + " " + message;
    }
    
    public static StatusLine valueOf(InputStream in) throws IOException {
        int code = DataUtils.beb2ushort(in);
        String message = StringUtils.readString(in);
        
        return new StatusLine(code, message);
    }
}
