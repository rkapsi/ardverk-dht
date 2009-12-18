package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    
    public DefaultMessageDispatcher(Transport transport, 
            MessageFactory factory, MessageCodec codec, 
            RouteTable routeTable, Database database) {
        super(transport, factory, codec);
        
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
            RequestMessage request, ResponseMessage response, long time,
            TimeUnit unit) throws IOException {
        
        super.handleResponse(callback, request, response, time, unit);
        defaultHandler.handleResponse(request, response, time, unit);
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
    
    public static KUID foo = null;
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        List<SimpleDHT> list = new ArrayList<SimpleDHT>();
        for (int i = 0; i < 200; i++) {
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
        
        /*for (int i = 0; i < list.size(); i++) {
            SimpleDHT dht = list.get(i);
            System.out.println(dht.getRouteTable().select(dht.getContactId(), 1000).length);
        }*/
        
        SimpleDHT dht = new SimpleDHT(5000);
        dht.ping("localhost", 2000).get();
        
        // Bootstrap
        KUID contactId = dht.getContactId();
        dht.lookup(contactId).get();
        
        foo = contactId;
        Thread.sleep(1000L);
        
        Random generator = new Random();
        for (int i = 0; i < list.size(); i++) {
            //int index = generator.nextInt(list.size());
            int index = i;
            
            SimpleDHT bla = list.get(index);
            
            //System.out.println(index + "-a: " + contactId + " -> " + Arrays.asList(bla.getRouteTable().select(contactId, 1)));
            
            AsyncFuture<NodeEntity> future = bla.lookup(contactId);
            NodeEntity entity = future.get();
            System.out.println(index + "-b: " + contactId 
                    + " -> " + entity + ", " + entity.getContacts().length 
                    + " @ " + entity.getContacts()[0]);
            
            /*System.out.println(index + "-c: " + contactId 
                    + " -> " + entity.getContacts().length 
                    + " @ " + Arrays.asList(entity.getContacts()));*/
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
        
        private final Database database;
        
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
            
            database = new DefaultDatabase();
            
            Contact contact = routeTable.getLocalhost();
            
            transport = new MinaTransport(new InetSocketAddress(port));
            messageFactory = new DefaultMessageFactory(
                    MESSAGE_ID_SIZE, contact);
            
            messageDispatcher = new DefaultMessageDispatcher(
                    transport, messageFactory, CODEC, 
                    routeTable, database);
        }
        
        public KUID getContactId() {
            return contactId;
        }
        
        public RouteTable getRouteTable() {
            return routeTable;
        }
        
        public AsyncFuture<PingEntity> ping(String host, int port) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, host, port);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncFuture<PingEntity> ping(Contact dst) {
            AsyncProcess<PingEntity> process 
                = new PingResponseHandler(messageDispatcher, dst);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
        
        public AsyncFuture<NodeEntity> lookup(KUID key) {
            AsyncProcess<NodeEntity> process 
                = new NodeResponseHandler(messageDispatcher, routeTable, key);
            return EXECUTOR.submit(process, 30L, TimeUnit.SECONDS);
        }
    }
}
