package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncExecutors;
import org.ardverk.concurrent.AsyncFuture;
import org.slf4j.Logger;

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
            MessageFactory factory, MessageCodec codec) {
        super(transport, factory, codec);
        
        defaultHandler = new DefaultMessageHandler();
        ping = new PingRequestHandler(this);
        node = new NodeRequestHandler(this);
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
        
        Transport transport = new MinaTransport(new InetSocketAddress(6666));
        MessageFactory factory = new DefaultMessageFactory(20);
        MessageCodec codec = new BencodeMessageCodec();
        MessageDispatcher messageDispatcher 
            = new DefaultMessageDispatcher(transport, factory, codec);
        
        for (int i = 0; i < 10; i++) {
            System.out.println("Sending: " + i);
            
            PingResponseHandler handler 
                = new PingResponseHandler(
                    messageDispatcher, 
                    "localhost", 6666);
        
            AsyncFuture<?> future = executor.submit(handler);
            Object value = future.get();
            System.out.println("Value: " + value);
        }
    }
}
