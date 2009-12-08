package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.message.DefaultPingRequest;
import com.ardverk.dht.message.MessageId;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;
import com.ardverk.utils.NetworkUtils;

public class PingResponseHandler extends ResponseHandler<PingEntity> {

    private static final Random GENERATOR = new Random();
    
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
    public void handleResponse(ResponseMessage response, 
            long time, TimeUnit unit) throws IOException {
        System.out.println("RESPONSE: " + response + ", " + time + ", " + unit);
        
        PingEntity entity = new PingEntity() {
            
        };
        
        setValue(entity);
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
            byte[] id = new byte[20];
            GENERATOR.nextBytes(id);
            
            MessageId messageId = new MessageId(id);
            KUID contactId = new KUID(new byte[] { 4, 5, 6 });
            
            Contact contact = new DefaultContact(Type.SOLICITED, 
                    contactId, 0, new InetSocketAddress("localhost", 6666));
            
            RequestMessage message = new DefaultPingRequest(
                    messageId, contact, address);
            
            messageDispatcher.send(PingResponseHandler.this, 
                    message, 10L, TimeUnit.SECONDS);
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
            RequestMessage message = null;
            messageDispatcher.send(PingResponseHandler.this, 
                    message, 10L, TimeUnit.SECONDS);
        }
    }
}
