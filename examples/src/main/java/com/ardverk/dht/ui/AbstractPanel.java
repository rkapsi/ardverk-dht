package com.ardverk.dht.ui;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.MessageListener;
import com.ardverk.dht.message.Message;

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
