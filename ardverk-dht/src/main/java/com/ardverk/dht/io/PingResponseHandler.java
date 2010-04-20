package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.entity.DefaultPingEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact2;
import com.ardverk.utils.NetworkUtils;

public class PingResponseHandler extends AbstractResponseHandler<PingEntity> {
    
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
            Contact2 contact) {
        super(messageDispatcher);
        
        sender = new ContactPingSender(contact);
    }
    
    @Override
    protected void go(AsyncFuture<PingEntity> future) throws IOException {
        sender.ping();
    }
    
    @Override
    protected void processResponse(RequestMessage request, 
            ResponseMessage response, long time, TimeUnit unit) {
        setValue(new DefaultPingEntity((PingResponse)response, time, unit));
    }
    
    @Override
    protected void processTimeout(RequestMessage request, 
            long time, TimeUnit unit) throws IOException {
        setException(new TimeoutIoException(request, time, unit));
    }
    
    private long getTimeoutInMillis() {
        return future.getTimeout(TimeUnit.MILLISECONDS);
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
            send(request, getTimeoutInMillis(), TimeUnit.MILLISECONDS);
        }
    }
    
    private class ContactPingSender implements PingSender {
        
        private final Contact2 contact;
        
        public ContactPingSender(Contact2 contact) {
            if (contact == null) {
                throw new NullPointerException("contact");
            }
            
            this.contact = contact;
        }
        
        @Override
        public void ping() throws IOException {
            MessageFactory factory = messageDispatcher.getMessageFactory();
            PingRequest request = factory.createPingRequest(contact);
            send(request, getTimeoutInMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
