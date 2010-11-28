package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;

public class NoSuchNodeException extends IOException {
    
    private static final long serialVersionUID = -2301202118771105303L;
    
    private final Outcome coutcome;
    
    NoSuchNodeException(Outcome coutcome) {
        this.coutcome = coutcome;
    }

    public Outcome getOutcome() {
        return coutcome;
    }
}