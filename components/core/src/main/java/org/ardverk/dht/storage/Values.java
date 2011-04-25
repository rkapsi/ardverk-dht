package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;

import org.ardverk.io.IoUtils;

public class Values {

    private final ResourceId[] resourceIds;
    
    public Values(ResourceId[] resourceIds) {
        this.resourceIds = resourceIds;
    }
    
    public ResourceId[] getResourceIds() {
        return resourceIds;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (ResourceId resourceId : resourceIds) {
            sb.append(resourceId).append("\n");
        }
        
        return sb.toString();
    }
    
    public Resource toResource(ResourceId resourceId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            
            out.writeShort(resourceIds.length);
            for (ResourceId rsrc: resourceIds) {
                out.writeUTF(rsrc.getURI().toString());
            }
            out.close();
            
            return new DefaultResource(resourceId, baos.toByteArray());
        } catch (IOException err) {
            throw new IllegalStateException("IOException", err);
        }
    }
    
    public static Values create(Resource resource) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(resource.getContent());
            
            int count = in.readUnsignedShort();
            ResourceId[] resourceIds = new ResourceId[count];
            for (int i = 0; i < resourceIds.length; i++) {
                URI uri = URI.create(in.readUTF());
                resourceIds[i] = DefaultResourceId.valueOf(uri);
            }
            
            return new Values(resourceIds);
        } catch (IOException err) {
            throw new IllegalStateException("IOException", err);
        } finally {
            IoUtils.close(in);
        }
    }
}
