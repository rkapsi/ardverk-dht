package org.ardverk.dht.storage;

class ResponseFactory {

    private ResponseFactory() {}
    
    public static Response createNotFound() {
        return new Response(StatusLine.NOT_FOUND);
    }
}
