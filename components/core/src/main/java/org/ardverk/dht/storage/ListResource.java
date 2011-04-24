package org.ardverk.dht.storage;

import java.util.List;

public interface ListResource extends Resource {

    public List<ResourceId> getResourceIds();
}
