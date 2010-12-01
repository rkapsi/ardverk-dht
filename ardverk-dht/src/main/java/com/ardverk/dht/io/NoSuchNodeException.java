package com.ardverk.dht.io;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;

public class NoSuchNodeException extends AbstractLookupException {
    
    private static final long serialVersionUID = -2301202118771105303L;
    
    public NoSuchNodeException(Outcome outcome) {
        super(outcome);
    }
}