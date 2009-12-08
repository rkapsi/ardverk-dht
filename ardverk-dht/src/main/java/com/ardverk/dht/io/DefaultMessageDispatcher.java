package com.ardverk.dht.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;

import org.ardverk.concurrent.AsyncExecutorService;
import org.ardverk.concurrent.AsyncExecutors;
import org.ardverk.concurrent.AsyncFuture;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.mina.MinaTransport;
import com.ardverk.dht.io.transport.Transport;
import com.ardverk.dht.message.BencodeMessageCodec;
import com.ardverk.dht.message.DefaultMessageFactory;
import com.ardverk.dht.message.DefaultPingResponse;
import com.ardverk.dht.message.MessageCodec;
import com.ardverk.dht.message.MessageFactory;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.ResponseMessage;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;

public class DefaultMessageDispatcher extends MessageDispatcher {

    public DefaultMessageDispatcher(Transport transport, 
            MessageFactory factory, MessageCodec codec) {
        super(transport, factory, codec);
    }

    @Override
    protected void handleRequest(SocketAddress src, 
            RequestMessage message) throws IOException {
        
        System.out.println("REQUEST: " + src + ", " + message);
        
        KUID contactId = new KUID(new byte[] { 7, 8, 9 });
        
        Contact contact = new DefaultContact(Type.SOLICITED, 
                contactId, 0, new InetSocketAddress("localhost", 6666));
        
        ResponseMessage response = new DefaultPingResponse(
                message.getMessageId(), contact, src);
        
        send(response);
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
            future.get();
            System.out.println("Done!");
        }
    }
}
