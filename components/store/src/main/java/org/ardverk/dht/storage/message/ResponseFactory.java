package org.ardverk.dht.storage.message;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.Header;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Constants;
import org.ardverk.utils.StringUtils;

public class ResponseFactory {

    private ResponseFactory() {}
    
    public static Response notFound() {
        return new Response(StatusLine.NOT_FOUND);
    }

    public static Response ok(Header... headers) {
        Response response = new Response(StatusLine.OK);
        response.addHeaders(headers);
        return response;
    }

    public static Response ok(Context context, ValueEntity value) {
        return new Response(StatusLine.OK, context, value);
    }
    
    public static Response list(StatusLine status, Key key, Map<? extends KUID, ? extends Context> values) {
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
            
            return new Response(status, 
                    new ByteArrayValueEntity(
                            Constants.XML_TEXT_TYPE, 
                            baos.toByteArray()));
        } catch (UnsupportedEncodingException err) {
            throw new IllegalStateException("UnsupportedEncodingException", err);
        } catch (XMLStreamException err) {
            throw new IllegalStateException("XMLStreamException", err);
        }
    }
}
