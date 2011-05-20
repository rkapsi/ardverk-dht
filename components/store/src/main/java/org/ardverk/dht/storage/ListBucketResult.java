package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.utils.StringUtils;

public class ListBucketResult extends BasicObjectValue {

    private static final String XML_VERSION = "1.0";
    
    public static final String XML_TEXT_TYPE = "text/xml";
    
    private final String name;
    
    private final String prefix;
    
    private final String marker;
    
    private final String delimiter;
    
    private final int maxKeys;
    
    private final Iterable<? extends Context> objects;
    
    public ListBucketResult(String name, String prefix, String marker, 
            String delimiter, int maxKeys, Iterable<? extends Context> objects) {
        this.name = name;
        this.prefix = prefix;
        this.marker = marker;
        this.delimiter = delimiter;
        this.maxKeys = maxKeys;
        this.objects = objects;
    }
    
    @Override
    protected void writeHeaders(ValueOutputStream out) throws IOException {
        
        if (!containsHeader(HTTP.CONTENT_TYPE)) {
            setHeader(HTTP.CONTENT_TYPE, XML_TEXT_TYPE);
        }
        
        super.writeHeaders(out);
    }
    
    @Override
    protected void writeContent(ValueOutputStream out) throws IOException {
        super.writeContent(out);
        
        try {
            writeXml(out);
        } catch (XMLStreamException e) {
            throw new IOException("XMLStreamException", e);
        }
    }

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
            
            for (Context context : objects) {
                xml.writeStartElement("Contents");
                
                xml.writeStartElement("Key");
                xml.writeCharacters(context.getKey().toString());
                xml.writeEndElement();
                
                xml.writeStartElement("LastModified");
                xml.writeCharacters(Long.toString(context.getLastModified()));
                xml.writeEndElement();
                
                xml.writeStartElement("Size");
                xml.writeCharacters(Long.toString(context.getSize()));
                xml.writeEndElement();
                
                xml.writeStartElement("ETag");
                xml.writeCharacters(context.getETag());
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
