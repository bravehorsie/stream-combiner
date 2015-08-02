package net.grigoriadi.sc.processing.generation;

import net.grigoriadi.sc.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Generates dummy stream for clients.
 */
public class StaxStreamGenerator extends AbstractStreamGenerator {

    private static Logger LOG = LoggerFactory.getLogger(StaxStreamGenerator.class);

    public StaxStreamGenerator() {
    }

    public StaxStreamGenerator(IItemWrittenListener itemWrittenListener) {
        super(itemWrittenListener);
    }

    @Override
    public void writeStream(OutputStream out) {
        XMLStreamWriter writer = null;
        try {
            XMLOutputFactory output = XMLOutputFactory.newInstance();
            writer = output.createXMLStreamWriter(out);

            writer.writeStartDocument();
            writer.setPrefix("grns", NS);
            writer.setDefaultNamespace(NS);
            writer.writeStartElement(NS, "Report");
            writer.writeDefaultNamespace(NS);

            for (long i=0; i < AppContext.getInstance().getGeneratedItemCountPerConnection() && !Thread.currentThread().isInterrupted(); i++) {
                writeItem(writer);
                writer.flush();
                desynchronizeTiming();
                simulateOccasionalServerHang();
            }

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (XMLStreamException e) {
            LOG.error("Stax error", e);
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }
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
