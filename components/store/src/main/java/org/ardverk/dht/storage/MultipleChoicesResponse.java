package org.ardverk.dht.storage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.VclockMap.Entry;
import org.ardverk.lang.Longs;
import org.ardverk.utils.StringUtils;

class MultipleChoicesResponse extends Response {

    private static final Comparator<VclockMap.Entry> COMPARATOR 
            = new Comparator<VclockMap.Entry>() {
        @Override
        public int compare(Entry o1, Entry o2) {
            
            Vclock vc1 = o1.getVclock();
            Vclock vc2 = o2.getVclock();
            
            return Longs.compare(
                    vc2.getLastModified(), 
                    vc1.getLastModified());
        }
    };
    
    public static Response create(Key key, VclockMap.Entry[] entries) {
        
        Arrays.sort(entries, COMPARATOR);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            
            XMLStreamWriter xml = factory.createXMLStreamWriter(
                    new OutputStreamWriter(baos, StringUtils.UTF8));
            
            try {
                xml.writeStartDocument();
                xml.writeStartElement("MultipleChoices");
                
                xml.writeStartElement("key");
                xml.writeCharacters(key.toString());
                xml.writeEndElement();
                
                xml.writeStartElement("values");
                for (VclockMap.Entry entry : entries) {
                    Vclock vclock = entry.getVclock();
                    
                    xml.writeStartElement("value");
                    
                    xml.writeStartElement("vtag");
                    xml.writeCharacters(vclock.vtag64());
                    xml.writeEndElement();
                    
                    xml.writeStartElement("creationTime");
                    xml.writeCharacters(DateUtils.format(vclock.getCreationTime()));
                    xml.writeEndElement();
                    
                    xml.writeStartElement("lastModified");
                    xml.writeCharacters(DateUtils.format(vclock.getLastModified()));
                    xml.writeEndElement();
                    
                    xml.writeEndElement(); // value
                }
                xml.writeEndElement(); // values
                xml.writeEndElement(); // MultipleChoices
                xml.writeEndDocument();
            } finally {
                xml.close();
            }
            
            return new MultipleChoicesResponse(StatusLine.MULTIPLE_CHOICES, 
                    new ByteArrayValueEntity(
                            Constants.XML_TEXT_TYPE, 
                            baos.toByteArray()));
            
        } catch (UnsupportedEncodingException err) {
            throw new IllegalStateException("UnsupportedEncodingException", err);
        } catch (XMLStreamException err) {
            throw new IllegalStateException("XMLStreamException", err);
        }
    }
    
    private MultipleChoicesResponse(StatusLine status, ValueEntity entity) {
        super(status, entity);
    }
}
