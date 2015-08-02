package net.grigoriadi.sc.processing;

import net.grigoriadi.stream_combiner.Item;
import net.grigoriadi.stream_combiner.ObjectFactory;
import net.grigoriadi.stream_combiner.Report;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Generates some data with JAXB.
 */
public class JaxbStreamGenerator extends AbstractStreamGenerator {

    public JaxbStreamGenerator() {
    }

    @Override
    public void writeStream(OutputStream out) throws InterruptedException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Item.class, Report.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            xsw.writeStartDocument();
            xsw.setDefaultNamespace(NS);
            xsw.writeStartElement(NS, "Report");

            ObjectFactory objectFactory = new ObjectFactory();
            for (int i = 0; i < 1000 && !Thread.currentThread().isInterrupted(); i++) {
                Item item = new Item();
                item.setTime(new Date().getTime());
                item.setAmount(BigDecimal.TEN);
                JAXBElement<Item> element = objectFactory.createItem(item);
                marshaller.marshal(element, xsw);
                xsw.flush();
                onItemWritten(item.getTime(), item.getAmount());
            }
            xsw.writeEndElement();
            xsw.writeEndDocument();
        } catch (JAXBException | XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
