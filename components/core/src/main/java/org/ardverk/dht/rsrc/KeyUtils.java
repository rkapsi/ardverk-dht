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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ardverk.utils.StringUtils;


public class KeyUtils {

    private KeyUtils() {}
    
    /**
     * @see #getKeyPath(URI)
     */
    public static String getKeyPath(Key key) {
        return getKeyPath(key.getURI());
    }
    
    /**
     * Returns the {@link URI}'s path. It's different from {@link URI#getPath()}
     * in the sense that the host and port are considered part of the path.
     */
    public static String getKeyPath(URI uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        int port = uri.getPort();
        
        return getKeyPath(host, port, path);
    }
    
    /**
     * @see #getKeyPath(URI)
     */
    public static String getKeyPath(String host, int port, String path) {
        // We consider the host:port part of the path.
        if (host != null && !host.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    host.length() + path.length() + 6);
            sb.append('/').append(host);
            if (port != -1) {
                sb.append(':').append(port);
            }
            return sb.append(path).toString();
        }
        
        return path;
    }
    
    public static Map<String, String> getQueryString(Key key) {
        return getQueryString(key.getURI());
    }
    
    public static Map<String, String> getQueryString(URI uri) {
        String query = uri.getQuery();
        if (query == null) {
            return Collections.emptyMap();
        }
        
        String[] arguments = query.split("&");
        Map<String, String> map = new HashMap<String, String>(arguments.length);
        
        for (String argument : arguments) {
            String[] tokens = argument.split("=");
            switch (tokens.length) {
                case 1:
                    map.put(decode(tokens[0]), "true");
                    break;
                case 2:
                    map.put(decode(tokens[0]), decode(tokens[1]));
                    break;
                default:
                    throw new IllegalArgumentException(argument);
            }
        }
        
        return map;
    }
    
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StringUtils.UTF_8);
        } catch (UnsupportedEncodingException err) {
            throw new IllegalArgumentException("UnsupportedEncodingException", err);
        }
    }
}
