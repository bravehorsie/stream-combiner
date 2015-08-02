package net.grigoriadi.sc.processing.parsing;


import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.stream_combiner.Report;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Created by rgrigoriadi on 8/1/15.
 */
public class JAXBParser implements IStreamParser {

    private XMLInputFactory xmlInputFactory;
    private final Consumer<Item> consumer;

    public JAXBParser(Consumer<Item> consumer) {
        this.consumer = consumer;
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    @Override
    public void readStream(InputStream stream) {
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(stream);
            JAXBContext jc = JAXBContext.newInstance(Item.class, Report.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            xmlEventReader.nextEvent();
            xmlEventReader.nextTag();

            while( xmlEventReader.peek().isStartElement() ) {
                JAXBElement<net.grigoriadi.stream_combiner.Item> item = unmarshaller.unmarshal(xmlEventReader, net.grigoriadi.stream_combiner.Item.class);
                net.grigoriadi.sc.domain.Item domainItem = new net.grigoriadi.sc.domain.Item();
                domainItem.setTime(item.getValue().getTime());
                domainItem.setAmount(item.getValue().getAmount());
                consumer.accept(domainItem);
            }
            System.out.println("Stream finished");
            xmlEventReader.close();
            
        } catch (JAXBException | XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
