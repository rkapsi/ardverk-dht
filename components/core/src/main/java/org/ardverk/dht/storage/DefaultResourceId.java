package org.ardverk.dht.storage;

import java.net.URI;

import org.ardverk.dht.KUID;

public class DefaultResourceId extends AbstractResourceId {

    private final KUID valueId;

    private final URI uri;
    
    private DefaultResourceId(KUID valueId, URI uri) {
        this.valueId = valueId;
        this.uri = uri;
    }
    
    @Override
    public KUID getId() {
        return valueId;
    }

    @Override
    public URI getURI() {
        return uri;
    }
    
    public static ResourceId valueOf(URI uri) {
        return new DefaultResourceId(parse(uri), uri);
    }
    
    public static ResourceId valueOf(KUID valueId) {
        return new DefaultResourceId(valueId, create(valueId));
    }
    
    private static URI create(KUID valueId) {
        return URI.create("ardverk:kuid:" + valueId.toHexString());
    }
    
    private static KUID parse(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals("ardverk")) {
            throw new IllegalArgumentException();
        }
        
        String ssp = uri.getSchemeSpecificPart();
        if (!ssp.startsWith("kuid:")) {
            throw new IllegalArgumentException();
        }
        
        String valueId = ssp.substring("kuid:".length());
        return KUID.create(valueId, 16);
    }
}