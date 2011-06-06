package org.ardverk.dht.storage;

import org.apache.http.Header;

class ResponseFactory {

    private ResponseFactory() {}
    
    public static Response createNotFound() {
        return new Response(StatusLine.NOT_FOUND);
    }

    public static Response createOk(Header... headers) {
        Response response = new Response(StatusLine.OK);
        response.addHeaders(headers);
        return response;
    }

    public static Response createOk(Context context, ValueEntity value) {
        return new Response(StatusLine.OK, context, value);
    }
}
