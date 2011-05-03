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

package org.ardverk.dht.storage;

import java.net.URI;

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
        
        // We consider the host:port part of the path.
        if (host != null && !host.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    host.length() + path.length() + 6);
            sb.append('/').append(host);
            
            int port = uri.getPort();
            if (port != -1) {
                sb.append(':').append(port);
            }
            return sb.append(path).toString();
        }
        
        return path;
    }
}
