package com.ardverk.dht.message;

import java.io.IOException;
import java.net.SocketAddress;

public class BencodeMessageCodec extends MessageCodec {

    public static final String NAME = "bencode";

    public BencodeMessageCodec() {
        super(NAME);
    }

    @Override
    public Message decode(SocketAddress src, byte[] in)
            throws IOException {
        return null;
    }

    @Override
    public byte[] encode(Message message, SocketAddress dst)
            throws IOException {
        return null;
    }
}
