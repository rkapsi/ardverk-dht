package com.ardverk.dht.io;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;

public class NoSuchValueException extends AbstractLookupException {
    
    private static final long serialVersionUID = -2753236114164880872L;
    
    public NoSuchValueException(Outcome outcome) {
        super(outcome);
    }
}