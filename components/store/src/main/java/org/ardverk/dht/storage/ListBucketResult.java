package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.io.InputOutputStream;
import org.ardverk.utils.StringUtils;

public class ListBucketResult extends AbstractValue {

    private final String name;
    
    private final String prefix;
    
    private final String marker;
    
    private final String delimiter;
    
    private final int maxKeys;
    
    private final Iterable<? extends Key> keys;
    
    public ListBucketResult(String name, String prefix, String marker, 
            String delimiter, int maxKeys, Iterable<? extends Key> keys) {
        this.name = name;
        this.prefix = prefix;
        this.marker = marker;
        this.delimiter = delimiter;
        this.maxKeys = maxKeys;
        this.keys = keys;
    }
    
    @Override
    public InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                try {
                    writeXml(out);
                } catch (XMLStreamException e) {
                    throw new IOException("XMLStreamException", e);
                }
            }
        };
    }
    
    private static final String XML_VERSION = "1.0";
    
    private void writeXml(OutputStream out) 
            throws IOException, XMLStreamException {
        
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter xml = factory.createXMLStreamWriter(
                new OutputStreamWriter(out, StringUtils.UTF_8));
        
        try {
            xml.writeStartDocument(StringUtils.UTF_8, XML_VERSION);
            xml.writeStartElement("ListBucketResult");
            
            xml.writeStartElement("Name");
            xml.writeCharacters(name);
            xml.writeEndElement();
            
            if (prefix != null) {
                xml.writeStartElement("Prefix");
                xml.writeCharacters(prefix);
                xml.writeEndElement();
            }
            
            if (marker != null) {
                xml.writeStartElement("Marker");
                xml.writeCharacters(marker);
                xml.writeEndElement();
            }
            
            if (delimiter != null) {
                xml.writeStartElement("Delimiter");
                xml.writeCharacters(delimiter);
                xml.writeEndElement();
            }
            
            if (0 < maxKeys) {
                xml.writeStartElement("MaxKeys");
                xml.writeCharacters(Integer.toString(maxKeys));
                xml.writeEndElement();
            }
            
            for (Key key : keys) {
                xml.writeStartElement("Contents");
                xml.writeStartElement("Key");
                xml.writeCharacters(key.toString());
                xml.writeEndElement();
                
                // More...
                
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeEndDocument();
        } finally {
            xml.close();
        }
    }
}
