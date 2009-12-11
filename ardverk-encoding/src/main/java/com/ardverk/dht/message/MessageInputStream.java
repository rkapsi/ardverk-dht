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
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.DefaultContact;
import com.ardverk.dht.routing.Contact.Type;
import com.ardverk.enumeration.IntegerValue;
import com.ardverk.enumeration.StringValue;

class MessageInputStream extends BencodingInputStream {
    
    public MessageInputStream(InputStream in) {
        super(in);
    }

    @Override
    public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
        if (IntegerValue.class.isAssignableFrom(clazz)) {
            int value = readInt();
            return readEnum(clazz, "valueOf", int.class, value);
        } else if (StringValue.class.isAssignableFrom(clazz)) {
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
    
    public Contact readContact(Contact.Type type, SocketAddress src) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        return new DefaultContact(type, contactId, 
                instanceId, src, address);
    }
    
    public Message readMessage(SocketAddress src) throws IOException {
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        OpCode opcode = readEnum(OpCode.class);
        MessageId messageId = readMessageId();
        Contact contact = readContact(opcode.isRequest() 
                ? Type.UNSOLICITED : Type.SOLICITED, src);
        SocketAddress address = readSocketAddress();
        
        switch (opcode) {
            case PING_REQUEST:
                return new DefaultPingRequest(messageId, 
                        contact, address);
            case PING_RESPONSE:
                return new DefaultPingResponse(messageId, 
                        contact, address);
            case FIND_NODE_REQUEST:
            case FIND_NODE_RESPONSE:
            case FIND_VALUE_REQUEST:
            case FIND_VALUE_RESPONSE:
            case STORE_REQUEST:
            case STORE_RESPONSE:
                throw new IllegalArgumentException("IMPLEMENT=" + opcode);
        }
        
        throw new IOException("opcode=" + opcode);
    }
}
