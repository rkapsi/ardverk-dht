package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message deserialize(SocketAddress src, byte[] data)
            throws IOException {
        return null;
    }

    @Override
    public byte[] serialize(Message message, SocketAddress dst)
            throws IOException {
        return null;
    }
}
