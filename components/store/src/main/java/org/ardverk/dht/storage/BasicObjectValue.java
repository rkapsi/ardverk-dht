package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.InputOutputStream;
import org.ardverk.io.IoUtils;

public class BasicObjectValue extends AbstractValue implements ObjectValue {

    protected final HeaderGroup headers;
    
    public BasicObjectValue() {
        this(new HeaderGroup());
    }
    
    public BasicObjectValue(HeaderGroup headers) {
        this.headers = headers;
    }
    
    @Override
    public boolean containsHeader(String name) {
        return headers.containsHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void addHeader(Header header) {
        headers.addHeader(header);
    }

    @Override
    public Header[] getAllHeaders() {
        return headers.getAllHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        return headers.getFirstHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return headers.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return headers.getLastHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(Header header) {
        headers.updateHeader(header);
    }

    @Override
    public void setHeaders(Header... h) {
        headers.setHeaders(h);
    }

    @Override
    public void removeHeaders(String name) {
        for (Header header : getHeaders(name)) {
            removeHeader(header);
        }
    }

    @Override
    public void removeHeader(Header header) {
        headers.removeHeader(header);
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                ValueOutputStream vos = new ValueOutputStream(out);
                try {
                    writeHeaders(vos);
                    writeContent(vos);
                } finally {
                    IoUtils.close(vos);
                }
            }
        };
    }
    
    protected void writeHeaders(ValueOutputStream out) throws IOException {
        out.writeHeaderGroup(headers);
    }
    
    protected void writeContent(ValueOutputStream out) throws IOException {
        
    }

    @Override
    public final void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);
    }
}
