package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.utils.NetworkUtils;

public class PingResponseHandler extends ResponseHandler<PingEntity> {
    
    private static final long TIMEOUT = 10L; // SECONDS
    
    private final PingSender sender;
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            String address, int port) {
        this(messageDispatcher, new InetSocketAddress(address, port));
    }
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            InetAddress address, int port) {
        this(messageDispatcher, new InetSocketAddress(address, port));
    }
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            SocketAddress address) {
        super(messageDispatcher);
        
        sender = new SocketAddressPingSender(address);
    }
    
    public PingResponseHandler(MessageDispatcher messageDispatcher, 
            Contact contact) {
        super(messageDispatcher);
        
        sender = new ContactPingSender(contact);
    }
    
    @Override
    protected void innerStart(AsyncFuture<PingEntity> future) throws IOException {
        System.out.println("PING");
        sender.ping();
    }
    
    @Override
    public void handleResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) throws IOException {
        System.out.println("RESPONSE: " + response + ", " + time + ", " + unit);
        
        setValue(new PingEntity(time, unit));
    }
    
    @Override
    public void handleTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        System.out.println("TIMEOUT: " + request + ", " + time + ", " + unit);
        
        setException(new TimeoutException());
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
            
            if (!NetworkUtils.isValidPort(address)) {
                throw new IllegalArgumentException("address=" + address);
            }
            
            this.address = address;
        }
    
        @Override
        public void ping() throws IOException {
            MessageFactory factory = messageDispatcher.getMessageFactory();
            PingRequest request = factory.createPingRequest(address);
            messageDispatcher.send(PingResponseHandler.this, 
                    request, TIMEOUT, TimeUnit.SECONDS);
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
            MessageFactory factory = messageDispatcher.getMessageFactory();
            PingRequest request = factory.createPingRequest(contact);
            messageDispatcher.send(PingResponseHandler.this, 
                    request, TIMEOUT, TimeUnit.SECONDS);
        }
    }
}
