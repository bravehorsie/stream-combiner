package net.grigoriadi.sc.processing;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * Generates dummy stream for clients.
 */
public class StaxStreamGenerator implements IStreamGenerator {

    @Override
    public void writeStream(OutputStream out) throws InterruptedException {
        try {
            XMLOutputFactory output = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = output.createXMLStreamWriter(out);

            writer.writeStartDocument();
            writer.setPrefix("grns", NS);
            writer.setDefaultNamespace(NS);
            writer.writeStartElement(NS, "Report");
            writer.writeDefaultNamespace(NS);

            for (long i=0; i<10000 && !Thread.currentThread().isInterrupted(); i++) {
                writeItem(writer);
                writer.flush();
                //desynchronize timings
                if (i % 100 == 0) {
                    Thread.sleep(new Random().nextInt(10));
                }
            }

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

    }


    private void writeItem(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("Item");
        writer.writeStartElement("time");
        String time = String.valueOf(new Date().getTime());
        writer.writeCharacters(time);
        writer.writeEndElement();
        writer.writeStartElement("amount");
        writer.writeCharacters(BigDecimal.TEN.toString());
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
