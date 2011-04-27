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

package org.ardverk.dht.http;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;

public class HttpUtils {

    private HttpUtils() {}
    
    /**
     * Calls close on the {@link ChannelEvent#getChannel()}.
     */
    public static void close(ChannelEvent event) {
        if (event != null) {
            close(event.getChannel());
        }
    }
    
    /**
     * Closes the given {@link Channel}.
     */
    public static void close(Channel channel) {
        if (channel != null) {
            channel.close();
        }
    }
}