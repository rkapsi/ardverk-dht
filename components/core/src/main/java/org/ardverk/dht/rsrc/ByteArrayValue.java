/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.rsrc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;

public class ByteArrayValue extends DefaultValue {
    
    private final byte[] data;
    
    private final int offset;
    
    private final int length;
    
    public ByteArrayValue(byte[] data) {
        this(data, 0, data.length);
    }
    
    public ByteArrayValue(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public int size() {
        return length;
    }
    
    public byte[] getContentAsBytes() {
        if (offset == 0 && length == data.length) {
            return data;
        }
        
        byte[] copy = new byte[length];
        System.arraycopy(data, offset, copy, 0, length);
        return copy;
    }
    
    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(data, offset, length);
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(data, offset, length);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public String toString() {
        return StringUtils.toString(getContentAsBytes());
    }
    
    public static ByteArrayValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static ByteArrayValue valueOf(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4*1-24];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } finally {
            IoUtils.close(baos);
        }
        return new ByteArrayValue(baos.toByteArray());
    }
}