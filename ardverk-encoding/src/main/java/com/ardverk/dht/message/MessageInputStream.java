package com.ardverk.dht.message;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.ardverk.coding.BencodingInputStream;

import com.ardverk.dht.KUID;
import com.ardverk.dht.io.session.SessionContext;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;
import com.ardverk.enumeration.IntegerValue;
import com.ardverk.enumeration.StringValue;

class MessageInputStream extends BencodingInputStream {

    private final SessionContext context;
    
    public MessageInputStream(InputStream in, SessionContext context) {
        super(in);
        
        if (context == null) {
            throw new NullPointerException("context");
        }
        
        this.context = context;
    }

    @Override
    public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
        if (clazz.isInstance(IntegerValue.class)) {
            int value = readInt();
            return readEnum(clazz, "valueOf", int.class, value);
        } else if (clazz.isInstance(StringValue.class)) {
            String value = readString();
            return readEnum(clazz, "from", String.class, value);
        } else {
            return super.readEnum(clazz);
        }
    }

    private static <T extends Enum<T>> T readEnum(Class<T> clazz, 
            String method, Class<?> type, Object value) throws IOException {
        try {
            Method m = clazz.getMethod(method, type);
            return clazz.cast(m.invoke(null, value));
        } catch (SecurityException e) {
            throw new IOException("SecurityException", e);
        } catch (IllegalArgumentException e) {
            throw new IOException("IllegalArgumentException", e);
        } catch (NoSuchMethodException e) {
            throw new IOException("NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
            throw new IOException("IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            throw new IOException("InvocationTargetException", e);
        }
    }

    public MessageId readMessageId() throws IOException {
        return new MessageId(readBytes());
    }
    
    public KUID readKUID() throws IOException {
        return new KUID(readBytes());
    }
    
    public InetAddress readInetAddress() throws IOException {
        return InetAddress.getByName(readString());
    }
    
    public SocketAddress readSocketAddress() throws IOException {
        String value = readString();
        int p = value.indexOf(':');
        return new InetSocketAddress(
                value.substring(0, p), 
                Integer.parseInt(value.substring(++p)));
    }
    
    public Contact readContact(Contact.Type type) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        return new DefaultContact(type, contactId, 
                instanceId, address);
    }
    
    public Message readMessage() throws IOException {
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        OpCode opcode = readEnum(OpCode.class);
        MessageId messageId = readMessageId();
        Contact contact = readContact(opcode.isRequest() 
                ? Type.UNSOLICITED : Type.SOLICITED);
        long time = readLong();
        
        InetAddress address = readInetAddress();
        
        switch (opcode) {
            case PING_REQUEST:
                return new DefaultPingRequest(messageId, 
                        contact, time, address);
            case PING_RESPONSE:
                return new DefaultPingResponse(messageId, 
                        contact, time, address);
        }
        
        throw new IOException("opcode=" + opcode);
    }
}
