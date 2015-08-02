package net.grigoriadi.sc.processing.parsing;

import net.grigoriadi.sc.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Reads an input stream and maps data to domain objects.
 * See {@link net.grigoriadi.stream_combiner.ObjectFactory}
 */
public class StaxParser implements IStreamParser {

    private static Logger LOG = LoggerFactory.getLogger(StaxParser.class);

    private Item currentItem;

    private StringBuilder content = new StringBuilder();

    private final Consumer<Item> consumer;

    public StaxParser(Consumer<Item> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void readStream(InputStream inputStream) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                reader.next();
                switch (reader.getEventType()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case ITEM_NAME:
                                currentItem = new Item();
                                break;
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        content.append(reader.getText().trim());
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case ITEM_NAME:
                                consumer.accept(currentItem);
                                content = new StringBuilder();
                                break;
                            case TIME_NAME:
                                currentItem.setTime(Long.valueOf(content.toString()));
                                content = new StringBuilder();
                                break;
                            case AMOUNT_NAME:
                                currentItem.setAmount(new BigDecimal(content.toString()));
                                content = new StringBuilder();
                                break;
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing stream error", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                inputStream.close();
            } catch (IOException | XMLStreamException e) {
                LOG.error("Error closing streams", e);
            }
        }
    }

}
