package com.ardverk.coding;

public abstract class AbstractBaseCoder implements BaseCoder {

    @Override
    public byte[] decode(byte[] data) {
        return decode(data, 0, data.length);
    }

    @Override
    public byte[] encode(byte[] data) {
        return encode(data, 0, data.length);
    }
}
