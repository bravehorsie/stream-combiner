package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.stream_combiner.ObjectFactory;
import net.grigoriadi.stream_combiner.Report;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Marshalls data as JSON, using EclipseLink JAXB.
 */
public class JsonDataMarshaller implements IDataMarshaller {

    private Marshaller marshaller;

    private ObjectFactory objectFactory = new ObjectFactory();

    public JsonDataMarshaller() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
        try {
            JAXBContext jc = JAXBContext.newInstance(new Class[]{net.grigoriadi.stream_combiner.Item.class, Report.class}, properties);
            marshaller = jc.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating JAXB context", e);
        }
    }

    @Override
    public void marshallData(Item data) {
            net.grigoriadi.stream_combiner.Item item = new net.grigoriadi.stream_combiner.Item();
            item.setAmount(data.getAmount());
            item.setTime(data.getTime());
            try {
                marshaller.marshal(objectFactory.createItem(item), new BufferedOutputStream(System.out));
                System.out.print("\n");
            } catch (JAXBException e) {
                throw new RuntimeException("Error marshalling data", e);
            }
    }
}
