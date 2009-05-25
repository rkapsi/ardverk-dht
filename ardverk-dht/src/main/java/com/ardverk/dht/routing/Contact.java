package com.ardverk.dht.routing;

import java.io.Serializable;

import com.ardverk.dht.KUID;

public interface Contact extends Serializable {

    public KUID getId();
}
