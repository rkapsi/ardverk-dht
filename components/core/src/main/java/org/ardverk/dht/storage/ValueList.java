package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.codec.bencode.MessageOutputStream;
import org.ardverk.dht.message.AbstractContent;
import org.ardverk.dht.message.Content;
import org.ardverk.io.IoUtils;

public class ValueList extends AbstractContent {

    private final ResourceId[] resourceIds;
    
    private byte[] payload = null;
    
    public ValueList(Collection<? extends ResourceId> c) {
        this(CollectionUtils.toArray(c, ResourceId.class));
    }
    
    public ValueList(ResourceId[] resourceIds) {
        this.resourceIds = resourceIds;
    }
    
    public ResourceId[] getResourceIds() {
        return resourceIds;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        int index = 0;
        for (ResourceId resourceId : resourceIds) {
            sb.append(index++).append(") ").append(resourceId).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public long getContentLength() {
        return payload().length;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new ByteArrayInputStream(payload());
    }
    
    private synchronized byte[] payload() {
        if (payload == null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                MessageOutputStream out = new MessageOutputStream(baos);
                
                out.writeShort(resourceIds.length);
                for (ResourceId resourceId: resourceIds) {
                    out.writeResourceId(resourceId);
                }
                out.close();
                
                payload = baos.toByteArray();
            } catch (IOException err) {
                throw new IllegalStateException("IOException", err);
            }
        }
        return payload;
    }
    
    public static ValueList valueOf(Content content) {
        MessageInputStream in = null;
        try {
            in = new MessageInputStream(content.getContent());
            
            int count = in.readUnsignedShort();
            ResourceId[] resourceIds = new ResourceId[count];
            for (int i = 0; i < resourceIds.length; i++) {
                resourceIds[i] = in.readResourceId();
            }
            
            return new ValueList(resourceIds);
        } catch (IOException err) {
            throw new IllegalStateException("IOException", err);
        } finally {
            IoUtils.close(in);
        }
    }
}
