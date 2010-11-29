package com.ardverk.dht.io;

import java.io.IOException;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;

public class NoSuchValueException extends IOException {
    
    private static final long serialVersionUID = -2753236114164880872L;

    private final Outcome outcome;
    
    NoSuchValueException(Outcome outcome) {
        this.outcome = outcome;
    }

    public Outcome getOutcome() {
        return outcome;
    }
}