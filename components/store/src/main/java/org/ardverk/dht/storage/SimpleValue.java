package org.ardverk.dht.storage;

import java.io.IOException;

import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.lang.IntegerValue;
import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;

public abstract class SimpleValue extends AbstractValue {

    public static final int VERSION = 0;
    
    public static enum ValueType implements IntegerValue {
        STATUS(0x01),
        KEY_LIST(0x02),
        BLOB(0x03);
        
        private final int type;
        
        private ValueType(int type) {
            this.type = type;
        }
        
        @Override
        public int intValue() {
            return type;
        }
        
        public static ValueType valueOf(int type) {
            for (ValueType value : values()) {
                if (value.type == type) {
                    return value;
                }
            }
            throw new IllegalArgumentException("type=" + type);
        }
    }
    
    private final ValueType valueType;
    
    public SimpleValue(ValueType valueType) {
        this.valueType = valueType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }
    
    protected void writeHeader(MessageOutputStream out) throws IOException {
        out.writeByte(VERSION);
        out.writeInt(getValueType().intValue());
    }
    
    public static SimpleValue valueOf(Value value) throws IOException {
        MessageInputStream in = null;
        try {
            in = new MessageInputStream(value.getContent());
            
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new IOException();
            }
            
            ValueType type = ValueType.valueOf(in.readInt());
            switch (type) {
                case STATUS:
                    return Status.valueOf(in);
                case KEY_LIST:
                    return KeyList.valueOf(in);
                case BLOB:
                    return BlobValue.valueOf(in);
                default:
                    throw new IOException();
            }
        } finally {
            IoUtils.close(in);
        }
    }
}
