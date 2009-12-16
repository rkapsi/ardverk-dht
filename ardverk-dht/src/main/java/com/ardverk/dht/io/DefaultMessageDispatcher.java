package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncExecutors;
import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;
import org.slf4j.Logger;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.DefaultKeyFactory;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.io.mina.MinaTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.BencodeMessageCodec;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.ContactFactory;
import com.ardverk.dht.routing.DefaultContactFactory;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.logging.LoggerUtils;

public class DefaultMessageDispatcher extends MessageDispatcher {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultMessageDispatcher.class);
    
    private final DefaultMessageHandler defaultHandler;
    
    private final PingRequestHandler ping;
    
    private final NodeRequestHandler node;
    
    private final ValueRequestHandler value;
    
    private final StoreRequestHandler store;
    
    public DefaultMessageDispatcher(Transport transport, 
            MessageFactory factory, MessageCodec codec, 
            RouteTable routeTable) {
        super(transport, factory, codec);
        
        defaultHandler = new DefaultMessageHandler(this, routeTable);
        ping = new PingRequestHandler(this);
        node = new NodeRequestHandler(this, routeTable);
        value = new ValueRequestHandler(this);
        store = new StoreRequestHandler(this);
    }

    @Override
    protected void handleRequest(RequestMessage request) throws IOException {
        
        defaultHandler.handleRequest(request);
        
        if (request instanceof PingRequest) {
            ping.handleRequest(request);
        } else if (request instanceof NodeRequest) {
            node.handleRequest(request);
        } else if (request instanceof ValueRequest) {
            value.handleRequest(request);
        } else if (request instanceof StoreRequest) {
            store.handleRequest(request);
        } else {
            unhandledRequest(request);
        }
    }
    
    @Override
    protected void handleResponse(MessageCallback callback,
            RequestMessage request, ResponseMessage response, long time,
            TimeUnit unit) throws IOException {
        
        super.handleResponse(callback, request, response, time, unit);
        defaultHandler.handleTimeout(request, time, unit);
    }

    @Override
    protected void handleTimeout(MessageCallback callback,
            RequestMessage request, long time, TimeUnit unit)
            throws IOException {
        
        super.handleTimeout(callback, request, time, unit);
        defaultHandler.handleTimeout(request, time, unit);
    }

    @Override
    protected void lateResponse(ResponseMessage response) throws IOException {
        defaultHandler.handleLateResponse(response);
    }

    protected void unhandledRequest(RequestMessage message) throws IOException {
        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled Request: " + message);
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        
        AsyncExecutorService executor = AsyncExecutors.newCachedThreadPool();
        
        ContactPinger pinger = new ContactPinger() {
            @Override
            public AsyncFuture<PingEntity> ping(Contact contact) {
                return null;
            }
        };
        
        KeyFactory keyFactory = new DefaultKeyFactory(20);
        ContactFactory contactFactory = new DefaultContactFactory(keyFactory);
        KUID contactId = keyFactory.createRandomKey();
        
        RouteTable routeTable = new DefaultRouteTable(pinger, 
                contactFactory, 20, contactId, 
                0, new InetSocketAddress("localhost", 6666));
        
        Contact contact = routeTable.getLocalhost();
        
        Transport transport = new MinaTransport(new InetSocketAddress(6666));
        MessageFactory factory = new DefaultMessageFactory(20, contact);
        MessageCodec codec = new BencodeMessageCodec();
        
        MessageDispatcher messageDispatcher 
            = new DefaultMessageDispatcher(transport, factory, codec, routeTable);
        
        for (int i = 0; i < 10; i++) {
            System.out.println("Sending: " + i);
            
            /*AsyncProcess<?> process 
                = new PingResponseHandler(
                    messageDispatcher, 
                    "localhost", 6666);*/
            
            KUID key = keyFactory.createRandomKey();
            AsyncProcess<?> process 
                = new NodeResponseHandler(
                    messageDispatcher, routeTable, key);
        
            AsyncFuture<?> future = executor.submit(process);
            Object value = future.get();
            System.out.println("Value: " + value);
        }
    }
    
    private static class SimpleDHT {
        
        private static final int KEY_SIZE = 20;
        
        private static final int MESSAGE_ID_SIZE = 20;
        
        private static final int K = 20;
        
        private static final AsyncExecutorService EXECUTOR 
            = AsyncExecutors.newCachedThreadPool();
        
        private static final KeyFactory KEY_FACTORY 
            = new DefaultKeyFactory(KEY_SIZE);
        
        private static final ContactFactory CONTACT_FACTORY 
            = new DefaultContactFactory(KEY_FACTORY);
        
        private static final MessageCodec CODEC 
            = new BencodeMessageCodec();
        
        private final KUID contactId;
        
        private final RouteTable routeTable;
        
        private final Transport transport;
        
        private final MessageFactory messageFactory;
        
        private final MessageDispatcher messageDispatcher;
        
        public SimpleDHT(int port) throws IOException {
            contactId = KEY_FACTORY.createRandomKey();
            
            ContactPinger pinger = new ContactPinger() {
                @Override
                public AsyncFuture<PingEntity> ping(Contact contact) {
                    return SimpleDHT.this.ping(contact);
                }
            };
            
            routeTable = new DefaultRouteTable(pinger, 
                    CONTACT_FACTORY, K, contactId, 
                    0, new InetSocketAddress("localhost", port));
            
            Contact contact = routeTable.getLocalhost();
            
            transport = new MinaTransport(new InetSocketAddress(port));
            messageFactory = new DefaultMessageFactory(
                    MESSAGE_ID_SIZE, contact);
            
            messageDispatcher = new DefaultMessageDispatcher(
                    transport, messageFactory, CODEC, routeTable);
        }
        
        public KUID getContactId() {
            return contactId;
        }
        
        public AsyncFuture<PingEntity> ping(String host, int port) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, host, port);
            return EXECUTOR.submit(process);
        }
        
        public AsyncFuture<PingEntity> ping(Contact dst) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, dst);
            return EXECUTOR.submit(process);
        }
        
        public AsyncFuture<NodeEntity> lookup(KUID key) {
            AsyncProcess<NodeEntity> process 
                = new NodeResponseHandler(messageDispatcher, routeTable, key);
            return EXECUTOR.submit(process);
        }
    }
}
