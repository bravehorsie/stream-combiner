package net.grigoriadi.sc.processing.parsing;


import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.stream_combiner.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Parses data using JAXB.
 */
public class JAXBParser implements IStreamParser {

    private static final Logger LOG = LoggerFactory.getLogger(JAXBParser.class);

    private XMLInputFactory xmlInputFactory;
    private final Consumer<Item> consumer;

    public JAXBParser(Consumer<Item> consumer) {
        this.consumer = consumer;
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    @Override
    public void readStream(InputStream stream) {
        XMLEventReader xmlEventReader = null;
        try {
            xmlEventReader = xmlInputFactory.createXMLEventReader(stream);
            JAXBContext jc = JAXBContext.newInstance(Item.class, Report.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            xmlEventReader.nextEvent();
            xmlEventReader.nextTag();

            while( xmlEventReader.peek().isStartElement() ) {
                JAXBElement<net.grigoriadi.stream_combiner.Item> item = unmarshaller.unmarshal(xmlEventReader, net.grigoriadi.stream_combiner.Item.class);
                net.grigoriadi.sc.domain.Item domainItem = new net.grigoriadi.sc.domain.Item(item.getValue().getTime(), item.getValue().getAmount());
                consumer.accept(domainItem);
            }
            xmlEventReader.close();

        } catch (JAXBException | XMLStreamException e) {
            LOG.error("Error parsing stream using JAXB", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (xmlEventReader != null) {
                    xmlEventReader.close();
                }
                stream.close();
            } catch (IOException | XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }
}
