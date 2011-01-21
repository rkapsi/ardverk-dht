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

package org.ardverk.dht.ui;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ardverk.dht.KUID;
import org.ardverk.dht.io.MessageListener;
import org.ardverk.dht.message.Message;


abstract class AbstractPanel extends JPanel implements MessageListener {
    
    private static final long serialVersionUID = 4832756148183064188L;

    @Override
    public final void handleMessageSent(final KUID contactId, final Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMessageSent0(contactId, message);
            }
        });
    }

    @Override
    public final void handleMessageReceived(final Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMessageReceived0(message);
            }
        });
    }
    
    protected abstract void handleMessageSent0(KUID contactId, Message message);
    
    protected abstract void handleMessageReceived0(Message message);
}