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
import com.ardverk.dht.routing.Contact2;
import com.ardverk.dht.storage.Database.Condition;
import com.ardverk.dht.storage.DefaultDatabase.DefaultCondition;
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
        return MessageId.create(readBytes());
    }
    
    public KUID readKUID() throws IOException {
        return KUID.create(readBytes());
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
    
    public Contact2 readContact(Contact2.Type type, SocketAddress src) throws IOException {
        KUID contactId = readKUID();
        int instanceId = readInt();
        SocketAddress address = readSocketAddress();
        
        if (type == Contact2.Type.UNKNOWN) {
            src = address;
        }
        
        return new Contact2(type, contactId, 
                instanceId, src, address);
    }
    
    public Contact2[] readContacts(Contact2.Type type, SocketAddress src) throws IOException {
        Contact2[] contacts = new Contact2[readInt()];
        for (int i = 0; i < contacts.length; i++) {
            contacts[i] = readContact(type, src);
        }
        return contacts;
    }
    
    public Condition readCondition() throws IOException {
        String value = readString();
        return DefaultCondition.valueOf(value);
    }
    
    public Message readMessage(SocketAddress src) throws IOException {
        int version = readUnsignedByte();
        if (version != MessageUtils.VERSION) {
            throw new IOException("version=" + version);
        }
        
        OpCode opcode = readEnum(OpCode.class);
        MessageId messageId = readMessageId();
        Contact2 contact = readContact(opcode.isRequest() 
                ? Contact2.Type.UNSOLICITED : Contact2.Type.SOLICITED, src);
        SocketAddress address = readSocketAddress();
        
        switch (opcode) {
            case PING_REQUEST:
                return readPingRequest(messageId, contact, address);
            case PING_RESPONSE:
                return readPingResponse(messageId, contact, address);
            case FIND_NODE_REQUEST:
                return readNodeRequest(messageId, contact, address);
            case FIND_NODE_RESPONSE:
                return readNodeResponse(messageId, contact, address);
            case FIND_VALUE_REQUEST:
                return readValueRequest(messageId, contact, address);
            case FIND_VALUE_RESPONSE:
                return readValueResponse(messageId, contact, address);
            case STORE_REQUEST:
                return readStoreRequest(messageId, contact, address);
            case STORE_RESPONSE:
                return readStoreResponse(messageId, contact, address);
            default:
                throw new IllegalArgumentException("opcode=" + opcode);
        }
    }
    
    private PingRequest readPingRequest(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        return new DefaultPingRequest(messageId, contact, address);
    }
    
    private PingResponse readPingResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        return new DefaultPingResponse(messageId, contact, address);
    }
    
    private NodeRequest readNodeRequest(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        KUID key = readKUID();
        return new DefaultNodeRequest(messageId, contact, address, key);
    }
    
    private NodeResponse readNodeResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        
        Contact2[] contacts = readContacts(Contact2.Type.UNKNOWN, address);
        return new DefaultNodeResponse(messageId, contact, address, contacts);
    }
    
    private ValueRequest readValueRequest(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        KUID key = readKUID();
        return new DefaultValueRequest(messageId, contact, address, key);
    }
    
    private ValueResponse readValueResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        byte[] value = readBytes();
        return new DefaultValueResponse(messageId, contact, address, value);
    }
    
    private StoreRequest readStoreRequest(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        KUID key = readKUID();
        byte[] value = readBytes();
        return new DefaultStoreRequest(messageId, contact, address, key, value);
    }
    
    private StoreResponse readStoreResponse(MessageId messageId, 
            Contact2 contact, SocketAddress address) throws IOException {
        Condition status = readCondition();
        return new DefaultStoreResponse(messageId, contact, address, status);
    }
}
