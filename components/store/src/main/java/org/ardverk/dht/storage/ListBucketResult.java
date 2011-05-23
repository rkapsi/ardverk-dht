package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.utils.StringUtils;

public class ListBucketResult extends ContextValue {

    private static final String XML_VERSION = "1.0";
    
    public static final String XML_TEXT_TYPE = "text/xml";
    
    private final String name;
    
    private final String prefix;
    
    private final String marker;
    
    private final String delimiter;
    
    private final int maxKeys;
    
    private final Map<? extends Key, ? extends Context> objects;
    
    public ListBucketResult(String name, String prefix, String marker, 
            String delimiter, int maxKeys, Map<? extends Key, ? extends Context> objects) {
        
        this.name = name;
        this.prefix = prefix;
        this.marker = marker;
        this.delimiter = delimiter;
        this.maxKeys = maxKeys;
        this.objects = objects;
        
        Context context = getContext();
        if (!context.containsHeader(HTTP.CONTENT_TYPE)) {
            context.setHeader(HTTP.CONTENT_TYPE, XML_TEXT_TYPE);
        }
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);
        
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
            
            for (Map.Entry<? extends Key, ? extends Context> entry : objects.entrySet()) {
                Key key = entry.getKey();
                Context value = entry.getValue();
                
                xml.writeStartElement("Contents");
                
                xml.writeStartElement("Key");
                xml.writeCharacters(key.toString());
                xml.writeEndElement();
                
                xml.writeStartElement("LastModified");
                xml.writeCharacters(Long.toString(value.getLastModified()));
                xml.writeEndElement();
                
                xml.writeStartElement("Size");
                xml.writeCharacters(Long.toString(value.getContentLength()));
                xml.writeEndElement();
                
                xml.writeStartElement("ETag");
                xml.writeCharacters(value.getETag());
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
