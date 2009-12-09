package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

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
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.logging.LoggerUtils;

public class DefaultMessageDispatcher extends MessageDispatcher {

    private static final Logger LOG 
        = LoggerUtils.getLogger(DefaultMessageDispatcher.class);
    
    private final PingRequestHandler ping;
    
    public DefaultMessageDispatcher(Transport transport, 
            MessageFactory factory, MessageCodec codec) {
        super(transport, factory, codec);
        
        ping = new PingRequestHandler(this);
    }

    @Override
    protected void handleRequest(RequestMessage message) throws IOException {
        
        if (message instanceof PingRequest) {
            ping.handleRequest(message);
        } else {
            unhandledRequest(message);
        }
    }
    
    protected void unhandledRequest(RequestMessage message) throws IOException {
        if (LOG.isErrorEnabled()) {
            LOG.error("Unhandled Request: " + message);
        }
    }
    
    @Override
    protected void lateResponse(ResponseMessage message) throws IOException {
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
