package com.ardverk.dht.io.process;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.io.MessageDispatcher;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.routing.Contact;

public class PingProcess extends AbstractProcess<PingResponse> {

    private final PingSender sender;
    
    public PingProcess(MessageDispatcher messageDispatcher, 
            String address, int port) {
        this(messageDispatcher, new InetSocketAddress(address, port));
    }
    
    public PingProcess(MessageDispatcher messageDispatcher, 
            InetAddress address, int port) {
        this(messageDispatcher, new InetSocketAddress(address, port));
    }
    
    public PingProcess(MessageDispatcher messageDispatcher, 
            SocketAddress address) {
        super(messageDispatcher);
        
        sender = new SocketAddressPingSender(address);
    }
    
    public PingProcess(MessageDispatcher messageDispatcher, 
            Contact contact) {
        super(messageDispatcher);
        
        sender = new ContactPingSender(contact);
    }

    @Override
    public void start(AsyncFuture<PingResponse> future) throws IOException {
        sender.ping();
    }
    
    private interface PingSender {
        public void ping() throws IOException;
    }
    
    private class SocketAddressPingSender implements PingSender {
        
        private final SocketAddress address;
        
        public SocketAddressPingSender(SocketAddress address) {
            if (address == null) {
                throw new NullPointerException("address");
            }
            
            /*if (!NetworkUtils.isValidPort(address)) {
                
            }*/
            
            this.address = address;
        }

        @Override
        public void ping() throws IOException {
        }
    }
    
    private class ContactPingSender implements PingSender {
        
        private final Contact contact;
        
        public ContactPingSender(Contact contact) {
            if (contact == null) {
                throw new NullPointerException("contact");
            }
            
            this.contact = contact;
        }
        
        @Override
        public void ping() throws IOException {
        }
    }
}
