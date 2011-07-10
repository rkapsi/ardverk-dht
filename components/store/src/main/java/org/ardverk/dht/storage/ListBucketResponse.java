package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ardverk.dht.rsrc.Key;
import org.ardverk.utils.StringUtils;

public class ListBucketResponse extends Response {

    public static Response create(Key prefix, Iterable<? extends Key> keys) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            
            XMLStreamWriter xml = factory.createXMLStreamWriter(
                    new OutputStreamWriter(baos, StringUtils.UTF8));
            
            try {
                xml.writeStartDocument();
                xml.writeStartElement("ListBucketResult");
                
                xml.writeStartElement("prefix");
                xml.writeCharacters(prefix.toString());
                xml.writeEndElement();
                
                for (Key key : keys) {
                    xml.writeStartElement("key");
                    xml.writeCharacters(key.toString());
                    xml.writeEndElement();
                }
                
                xml.writeEndElement();
                xml.writeEndDocument();
            } finally {
                xml.close();
            }
            
            return new ListBucketResponse(StatusLine.OK, 
                    new ByteArrayValueEntity(
                            Constants.XML_TEXT_TYPE, 
                            baos.toByteArray()));
            
        } catch (UnsupportedEncodingException err) {
            throw new IllegalStateException("UnsupportedEncodingException", err);
        } catch (XMLStreamException err) {
            throw new IllegalStateException("XMLStreamException", err);
        }
    }
    
    private ListBucketResponse(StatusLine status, ValueEntity entity) {
        super(status, entity);
    }
}
