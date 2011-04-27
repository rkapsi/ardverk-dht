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

package org.ardverk.dht.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.utils.StringUtils;

public class ByteArrayContent implements Content {
    
    private final byte[] content;
    
    private final int offset;
    
    private final int length;
    
    public ByteArrayContent(byte[] content) {
        this(content, 0, content.length);
    }
    
    public ByteArrayContent(byte[] content, int offset, int length) {
        this.content = content;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public DHTFuture<Void> getContentFuture() {
        return DEFAULT_FUTURE;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(content, offset, length);
    }

    @Override
    public byte[] getContentAsBytes() {
        if (offset == 0 && length == content.length) {
            return content;
        }
        
        byte[] copy = new byte[length];
        System.arraycopy(content, offset, copy, 0, length);
        return copy;
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
    
    public static ByteArrayContent valueOf(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4 * 1024];
        int len = -1;
        while ((len = in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return new ByteArrayContent(baos.toByteArray());
    }
}