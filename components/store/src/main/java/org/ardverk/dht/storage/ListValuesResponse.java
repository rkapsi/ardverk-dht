package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.sql.DefaultIndex2.Values;
import org.ardverk.utils.StringUtils;

class ListValuesResponse extends Response {
    
    public static Response create(StatusLine status, Key key, Values values) {
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
                for (Map.Entry<KUID, Context> entry : values.entrySet()) {
                    KUID valueId = entry.getKey();
                    Context context = entry.getValue();
                    
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
            
            return new ListValuesResponse(status, 
                    new ByteArrayValueEntity(
                            Constants.XML_TEXT_TYPE, 
                            baos.toByteArray()));
            
        } catch (UnsupportedEncodingException err) {
            throw new IllegalStateException("UnsupportedEncodingException", err);
        } catch (XMLStreamException err) {
            throw new IllegalStateException("XMLStreamException", err);
        }
    }
    
    private ListValuesResponse(StatusLine status, ValueEntity entity) {
        super(status, entity);
    }
}
