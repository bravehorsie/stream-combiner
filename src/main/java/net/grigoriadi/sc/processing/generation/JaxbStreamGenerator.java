package net.grigoriadi.sc.processing.generation;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.stream_combiner.Item;
import net.grigoriadi.stream_combiner.ObjectFactory;
import net.grigoriadi.stream_combiner.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Generates some data with JAXB.
 */
public class JaxbStreamGenerator extends AbstractStreamGenerator {

    private static Logger LOG = LoggerFactory.getLogger(JaxbStreamGenerator.class);

    public JaxbStreamGenerator() {
    }

    public JaxbStreamGenerator(IItemWrittenListener itemWrittenListener) {
        super(itemWrittenListener);
    }

    @Override
    public void writeStream(OutputStream out) {
        XMLStreamWriter xsw = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(Item.class, Report.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            xsw.writeStartDocument();
            xsw.setDefaultNamespace(NS);
            xsw.writeStartElement(NS, "Report");

            ObjectFactory objectFactory = new ObjectFactory();
            for (int i = 0; i < AppContext.getInstance().getGeneratedItemCountPerConnection() && !Thread.currentThread().isInterrupted(); i++) {
                Item item = new Item();
                item.setTime(new Date().getTime());
                item.setAmount(newAmount());
                JAXBElement<Item> element = objectFactory.createItem(item);
                marshaller.marshal(element, xsw);
//                xsw.flush();
                onItemWritten(item.getTime(), item.getAmount());
                desynchronizeTiming();
                simulateOccasionalServerHang();
                System.out.println();
            }
            xsw.writeEndElement();
            xsw.writeEndDocument();
            xsw.flush();
        } catch (JAXBException | XMLStreamException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (xsw != null) {
                    xsw.close();
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }
}
