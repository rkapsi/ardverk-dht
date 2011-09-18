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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.ardverk.dht.ArdverkDHT;
import org.ardverk.dht.DHT;
import org.ardverk.dht.KUID;
import org.ardverk.dht.io.MessageListener;
import org.ardverk.dht.message.Message;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.ui.Painter.EventType;


public class PainterPanel extends JPanel {
    
    private static final long serialVersionUID = -8406331670508490192L;

    private static final int FREQUENCY = 100;
    
    private final Timer timer = new Timer(FREQUENCY, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }
    });
    
    private final MessageListener listener = new MessageListener() {
        @Override
        public void handleMessageSent(KUID contactId, Message message) {
            PainterPanel.this.handleMessageSent(contactId, message);
        }
        
        @Override
        public void handleMessageReceived(Message message) {
            PainterPanel.this.handleMessageReceived(message);
        }
    };
    
    private final List<Painter> painters = new ArrayList<Painter>();
    
    private final DHT dht;
    
    private volatile int current = 0;
    
    public PainterPanel(DHT dht) {
        this.dht = dht;
        
        Contact localhost = dht.getIdentity();
        KUID localhostId = localhost.getId();
        
        painters.add(new JuicePainter(localhostId));
        painters.add(new SquashPainter(localhostId));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!painters.isEmpty()) {
                    current = (current + 1) % painters.size();
                    repaint();
                }
            }
        });
    }
    
    public boolean isRunning() {
        return timer.isRunning();
    }
    
    public void start() {
        if (!isRunning()) {
            timer.start();
            ((ArdverkDHT)dht).getMessageDispatcher().addMessageListener(listener);
        }
    }
    
    public void stop() {
        if (isRunning()) {
            timer.stop();
            ((ArdverkDHT)dht).getMessageDispatcher().removeMessageListener(listener);
        }
    }
    
    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        painter().paint(this, g);
    }
    
    private Painter painter() {
        int current = this.current;
        if (current < painters.size()) {
            return painters.get(current);
        }
        return Nop.INSTANCE;
    }
    
    private void handleMessageSent(KUID contactId, Message message) {
        handleEvent(EventType.MESSAGE_SENT, contactId, message);
    }

    private void handleMessageReceived(Message message) {
        handleEvent(EventType.MESSAGE_RECEIVED, 
                message.getContact().getId(), message);
    }
    
    private void handleEvent(EventType type, KUID contactId, Message message) {
        painter().handleEvent(type, contactId, message);
    }
    
    private static class Nop implements Painter {

        private static final Nop INSTANCE = new Nop();
        
        private Nop() {}
        
        @Override
        public void paint(Component c, Graphics g) {
        }

        @Override
        public void handleEvent(EventType type, 
                KUID contactId, Message message) {
        }
    }
}