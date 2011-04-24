package org.ardverk.dht.storage;


public abstract class AbstractResource implements Resource {

    private final ResourceId resourceId;
    
    public AbstractResource(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }
}
