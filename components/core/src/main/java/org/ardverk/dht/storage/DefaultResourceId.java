package org.ardverk.dht.storage;

import java.net.URI;

import org.ardverk.dht.KUID;

public class DefaultResourceId extends AbstractResourceId {

    private static final KUID BUCKET = KUID.createRandom(20);
    
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
        return URI.create("ardverk:///" + BUCKET.toHexString() + "/" + valueId.toHexString());
    }
    
    public static ResourceId valueOf(KUID valueId, String query) {
        return new DefaultResourceId(valueId, create(valueId, query));
    }
    
    public static URI create(KUID valueId, String query) {
        return URI.create("ardverk:///" + BUCKET.toHexString() + "/" + valueId.toHexString() + "?" + query);
    }
    
    private static KUID parse(URI uri) {
        String scheme = uri.getScheme();
        if (!scheme.equals("ardverk")) {
            throw new IllegalArgumentException();
        }
        
        String path = uri.getPath();
        while (!path.isEmpty() && path.startsWith("/")) {
            path = path.substring(1);
        }
        
        int p = path.indexOf("/");
        if (p != -1) {
            path = path.substring(0, p);
        }
        
        return KUID.create(path, 16);
    }
}