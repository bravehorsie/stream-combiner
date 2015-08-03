package net.grigoriadi.sc.processing.parsing;

import net.grigoriadi.sc.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Parsing the stream using SAX.
 */
public class SaxParser implements IStreamParser {

    private static Logger LOG = LoggerFactory.getLogger(SaxParser.class);

    private Item currentItem;

    private StringBuilder content = new StringBuilder();

    private final Consumer<Item> consumer;

    public SaxParser(Consumer<Item> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void readStream(InputStream inputStream) {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = parserFactory.newSAXParser();
            ItemSaxHaneler handler = new ItemSaxHaneler();
            parser.parse(inputStream, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Error parsing data with SAX", e);
            throw new RuntimeException(e);
        }

    }

    private class ItemSaxHaneler extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case ITEM_NAME:
                    currentItem = new Item();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case ITEM_NAME:
                    consumer.accept(currentItem);
                    content = new StringBuilder();
                    break;
                case TIME_NAME:
                    currentItem = new Item(Long.valueOf(content.toString()), currentItem.getAmount());
                    content = new StringBuilder();
                    break;
                case AMOUNT_NAME:
                    currentItem = new Item(currentItem.getTime(), new BigDecimal(content.toString()));
                    content = new StringBuilder();
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            content.append(String.copyValueOf(ch, start, length).trim());
        }
    }
}