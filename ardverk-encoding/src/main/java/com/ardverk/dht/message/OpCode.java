package com.ardverk.dht.message;


enum OpCode implements IntegerValue {
    
    PING_REQUEST(0x00),
    PING_RESPONSE(0x01),
    
    FIND_NODE_REQUEST(0x02),
    FIND_NODE_RESPONSE(0x03),
    
    FIND_VALUE_REQUEST(0x04),
    FIND_VALUE_RESPONSE(0x05),
    
    STORE_REQUEST(0x06),
    STORE_RESPONSE(0x07);
    
    private final int value;
    
    private OpCode(int value) {
        this.value = value;
    }
    
    @Override
    public int intValue() {
        return value;
    }
    
    public boolean isRequest() {
        switch (this) {
            case PING_REQUEST:
            case FIND_NODE_REQUEST:
            case FIND_VALUE_REQUEST:
            case STORE_REQUEST:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return name() + " (" + intValue() + ")";
    }
    
    private static final OpCode[] VALUES;
    
    static {
        OpCode[] values = values();
        VALUES = new OpCode[values.length];
        
        for (OpCode o : values) {
            int index = o.value % VALUES.length;
            if (VALUES[index] != null) {
                throw new IllegalStateException();
            }
            VALUES[index] = o;
        }
    }
    
    public static OpCode valueOf(int value) {
        int index = (value & Integer.MAX_VALUE) % VALUES.length;
        OpCode opcode = VALUES[index];
        if (opcode != null && opcode.value == value) {
            return opcode;
        }
        
        throw new IllegalArgumentException("value=" + value);
    }
    
    public static OpCode valueOf(Message message) {
        if (message instanceof PingRequest) {
            return PING_REQUEST;
        } else if (message instanceof PingResponse) {
            return PING_RESPONSE;
        } else if (message instanceof NodeRequest) {
            return FIND_NODE_REQUEST;
        } else if (message instanceof NodeResponse) {
            return FIND_NODE_RESPONSE;
        } else if (message instanceof ValueRequest) {
            return FIND_VALUE_REQUEST;
        } else if (message instanceof ValueResponse) {
            return FIND_VALUE_RESPONSE;
        } else if (message instanceof StoreRequest) {
            return STORE_REQUEST;
        } else if (message instanceof StoreResponse) {
            return STORE_RESPONSE;
        }
        
        throw new IllegalArgumentException("message=" + message);
    }
}