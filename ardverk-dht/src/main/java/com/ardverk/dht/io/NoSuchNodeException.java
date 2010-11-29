package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;

public class NoSuchNodeException extends IOException {
    
    private static final long serialVersionUID = -2301202118771105303L;
    
    private final Outcome outcome;
    
    NoSuchNodeException(Outcome outcome) {
        this.outcome = outcome;
    }

    public Outcome getOutcome() {
        return outcome;
    }
}