package com.ardverk.io;

import java.io.IOException;
import java.io.OutputStream;

public interface Writable {

    public int write(OutputStream out) throws IOException;
}
