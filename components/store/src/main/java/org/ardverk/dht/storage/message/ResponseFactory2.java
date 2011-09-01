package org.ardverk.dht.storage.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.StringValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.Constants;
import org.ardverk.lang.ExceptionUtils;
import org.ardverk.utils.StringUtils;

public class ResponseFactory2 {

    public static final Response2 INTERNAL_SERVER_ERROR = new Response2(
            StatusLine.INTERNAL_SERVER_ERROR);
    
    public static final Response2 LENGTH_REQUIRED = new Response2(
            StatusLine.LENGTH_REQUIRED);
    
    public static Response2 ok() {
        return new Response2(StatusLine.OK);
    }
    
    public static Response2 notFound() {
        return new Response2(StatusLine.NOT_FOUND);
    }
    
    public static Response2 error(Throwable t) {
        return error(ExceptionUtils.toString(t));
    }
    
    public static Response2 error(String message) {
        return new Response2(StatusLine.INTERNAL_SERVER_ERROR)
            .setContentType(HTTP.PLAIN_TEXT_TYPE)
            .setValue(new StringValue(message));
    }
    
    public static Value commit(ContextMessage<?> response) {
        try {
            return response.commit();
        } catch (IOException err) {
            throw new IllegalStateException("IOException", err);
        }
    }
    
    public static Response2 list(StatusLine status, Key key, Map<? extends KUID, ? extends Context> values) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            
            XMLStreamWriter xml = factory.createXMLStreamWriter(
                    new OutputStreamWriter(baos, StringUtils.UTF8));
            
            try {
                xml.writeStartDocument();
                xml.writeStartElement("List");
                
                xml.writeStartElement("key");
                xml.writeCharacters(key.toString());
                xml.writeEndElement();
                
                xml.writeStartElement("values");
                for (Map.Entry<? extends KUID, ? extends Context> entry : values.entrySet()) {
                    KUID valueId = entry.getKey();
                    //Context context = entry.getValue();
                    
                    xml.writeStartElement("value");
                    
                    xml.writeStartElement("id");
                    xml.writeCharacters(valueId.toHexString());
                    xml.writeEndElement();
                    
                    //xml.writeStartElement("creationTime");
                    //xml.writeCharacters(DateUtils.format(vclock.getCreationTime()));
                    //xml.writeEndElement();
                    
                    //xml.writeStartElement("lastModified");
                    //xml.writeCharacters(DateUtils.format(vclock.getLastModified()));
                    //xml.writeEndElement();
                    
                    xml.writeEndElement(); // value
                }
                xml.writeEndElement(); // values
                xml.writeEndElement(); // List
                xml.writeEndDocument();
            } finally {
                xml.close();
            }
            
            return new Response2(status)
                .setContentType(Constants.XML_TEXT_TYPE)
                .setValue(new ByteArrayValue(baos.toByteArray()));
            
        } catch (UnsupportedEncodingException err) {
            throw new IllegalStateException("UnsupportedEncodingException", err);
        } catch (XMLStreamException err) {
            throw new IllegalStateException("XMLStreamException", err);
        }
    }
    
    private ResponseFactory2() {}
}
