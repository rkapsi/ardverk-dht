package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncFuture;
import org.ardverk.concurrent.AsyncProcess;
import org.ardverk.concurrent.AsyncProcessExecutorService;
import org.ardverk.concurrent.AsyncProcessFuture;
import org.ardverk.concurrent.ExecutorUtils;
import org.slf4j.Logger;

import com.ardverk.dht.ContactPinger;
import com.ardverk.dht.DefaultKeyFactory;
import com.ardverk.dht.KUID;
import com.ardverk.dht.KeyFactory;
import com.ardverk.dht.entity.NodeEntity;
import com.ardverk.dht.entity.PingEntity;
import com.ardverk.dht.entity.StoreEntity;
import com.ardverk.dht.entity.ValueEntity;
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
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.routing.DefaultRouteTable;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.dht.routing.Contact2.Type;
import com.ardverk.dht.storage.Database;
import com.ardverk.dht.storage.DefaultDatabase;
import com.ardverk.logging.LoggerUtils;

public class DefaultMessageDispatcher extends MessageDispatcher {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultMessageDispatcher.class);
    
    private final DefaultMessageHandler defaultHandler;
    
    private final PingRequestHandler ping;
    
    private final NodeRequestHandler node;
    
    private final ValueRequestHandler value;
    
    private final StoreRequestHandler store;
    
    public DefaultMessageDispatcher(MessageFactory factory, 
            MessageCodec codec, RouteTable routeTable, 
            Database database) {
        super(factory, codec);
        
        defaultHandler = new DefaultMessageHandler(this, routeTable);
        ping = new PingRequestHandler(this);
        node = new NodeRequestHandler(this, routeTable);
        value = new ValueRequestHandler(this, routeTable, database);
        store = new StoreRequestHandler(this, routeTable, database);
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
            RequestEntity entity, ResponseMessage response, long time,
            TimeUnit unit) throws IOException {
        
        super.handleResponse(callback, entity, response, time, unit);
        defaultHandler.handleResponse(entity, response, time, unit);
    }

    @Override
    protected void handleTimeout(MessageCallback callback,
            RequestEntity entity, long time, TimeUnit unit)
            throws IOException {
        
        super.handleTimeout(callback, entity, time, unit);
        defaultHandler.handleTimeout(entity, time, unit);
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
        List<SimpleDHT> list = new ArrayList<SimpleDHT>();
        for (int i = 0; i < 100; i++) {
            list.add(new SimpleDHT(2000 + i));
        }
        
        for (int i = 0; i < list.size(); i++) {
            SimpleDHT dht = list.get(i);
            
            for (int j = 0; j < list.size(); j++) {
                if (j != i) {
                    dht.ping("localhost", 2000 + j).get();
                }
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            SimpleDHT dht = list.get(i);
            dht.lookup(dht.getContactId()).get();
        }
        
        SimpleDHT dht = new SimpleDHT(5000);
        dht.ping("localhost", 2000).get();
        
        // Bootstrap
        KUID contactId = dht.getContactId();
        dht.lookup(contactId).get();
        
        Thread.sleep(1000L);
        
        for (int i = 0; i < list.size(); i++) {
            SimpleDHT inst = list.get(i);
            
            AsyncFuture<NodeEntity> future = inst.lookup(contactId);
            NodeEntity entity = future.get();
            System.out.println(i + ": " + contactId 
                    + " -> " + entity + ", " + entity.getContacts().length 
                    + " @ " + entity.getContacts()[0]);
        }
        
        KUID key = SimpleDHT.KEY_FACTORY.createRandomKey();
        byte[] value = "Hello World!".getBytes();
        
        AsyncFuture<NodeEntity> future = dht.lookup(key);
        NodeEntity entity = future.get();
        
        System.out.println(entity.size());
        
        AsyncFuture<StoreEntity> store = dht.put(entity, key, value);
        StoreEntity storeEntity = store.get();
        
        System.out.println(storeEntity);
        
        for (int i = 0; i < 100; i++) {
            SimpleDHT foo = list.get((int)(Math.random() * list.size()));
            AsyncFuture<ValueEntity> valueFuture = foo.get(key);
            System.out.println(valueFuture.get().getValueAsString());
        }
    }
    
    private static class SimpleDHT {
        
        private static final int KEY_SIZE = 20;
        
        private static final int MESSAGE_ID_SIZE = 20;
        
        private static final int K = 20;
        
        private static final AsyncProcessExecutorService EXECUTOR 
            = ExecutorUtils.newCachedThreadPool("SimpleDHT");
        
        private static final KeyFactory KEY_FACTORY 
            = new DefaultKeyFactory(KEY_SIZE);
        
        private static final MessageCodec CODEC 
            = new BencodeMessageCodec();
        
        private final KUID contactId;
        
        private final RouteTable routeTable;
        
        private final Database database;
        
        private final Transport transport;
        
        private final MessageFactory messageFactory;
        
        private final MessageDispatcher messageDispatcher;
        
        public SimpleDHT(int port) throws IOException {
            contactId = KEY_FACTORY.createRandomKey();
            
            ContactPinger pinger = new ContactPinger() {
                @Override
                public AsyncFuture<PingEntity> ping(Contact2 contact, 
                        long timeout, TimeUnit unit) {
                    return SimpleDHT.this.ping(contact);
                }
            };
            
            SocketAddress address = new InetSocketAddress("localhost", port);
            Contact2 localhost = new Contact2(Type.SOLICITED, 
                    contactId, 0, address);
            
            routeTable = new DefaultRouteTable(pinger, K, localhost);
            
            database = new DefaultDatabase();
            
            Contact2 contact = routeTable.getLocalhost();
            
            transport = new MinaTransport(new InetSocketAddress(port));
            messageFactory = new DefaultMessageFactory(
                    MESSAGE_ID_SIZE, contact);
            
            messageDispatcher = new DefaultMessageDispatcher(
                    messageFactory, CODEC, 
                    routeTable, database);
            messageDispatcher.bind(transport);
        }
        
        public KUID getContactId() {
            return contactId;
        }
        
        public RouteTable getRouteTable() {
            return routeTable;
        }
        
        public AsyncProcessFuture<PingEntity> ping(String host, int port) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, host, port);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncProcessFuture<PingEntity> ping(Contact2 dst) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, dst);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncProcessFuture<NodeEntity> lookup(KUID key) {
            AsyncProcess<NodeEntity> process 
                = new NodeResponseHandler(messageDispatcher, routeTable, key);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncProcessFuture<ValueEntity> get(KUID key) {
            AsyncProcess<ValueEntity> process 
                = new ValueResponseHandler(messageDispatcher, routeTable, key);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncProcessFuture<StoreEntity> put(NodeEntity entity, KUID key, byte[] value) {
            AsyncProcess<StoreEntity> process 
                = new StoreResponseHandler(messageDispatcher, entity, key, value);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
    }
}
