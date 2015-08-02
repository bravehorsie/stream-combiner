package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppContext;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Generates dummy stream for clients.
 */
public class StaxStreamGenerator extends AbstractStreamGenerator {

    public StaxStreamGenerator() {
    }

    public StaxStreamGenerator(IItemWrittenListener itemWrittenListener) {
        super(itemWrittenListener);
    }

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

            for (long i=0; i < AppContext.GENERATED_ITEM_COUNT_PER_CONNECTION && !Thread.currentThread().isInterrupted(); i++) {
                writeItem(writer);
                writer.flush();
                desynchronizeTiming();
                simulateOccasionalServerHang();
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
        Long time = new Date().getTime();
        writer.writeCharacters(String.valueOf(time));
        writer.writeEndElement();
        writer.writeStartElement("amount");
        BigDecimal amount = newAmount();
        writer.writeCharacters(amount.toString());
        writer.writeEndElement();
        writer.writeEndElement();
        onItemWritten(time, amount);
    }
}
