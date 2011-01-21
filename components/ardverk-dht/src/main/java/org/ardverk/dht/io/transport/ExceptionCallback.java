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

package org.ardverk.dht.io.transport;

import org.ardverk.dht.message.Message;

/**
 * The {@link ExceptionCallback} is called if the {@link Transport}'s
 * {@link Transport#send(Message, ExceptionCallback)} method fails to
 * send the given {@link Message}.
 * 
 * @see Transport
 */
public interface ExceptionCallback {

    /**
     * Called if a transport error occurred.
     */
    public void handleException(Message message, Throwable t);
}
