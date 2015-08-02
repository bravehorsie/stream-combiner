package net.grigoriadi.sc.processing;

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
import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Parsing the stream using SAX.
 */
public class SaxParser implements IStreamParser {

    private static Logger LOG = LoggerFactory.getLogger(SaxParser.class);

    private Item currentItem;

    private String content;

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
            LOG.error("Sax error", e);
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
                    if (currentItem == null) {
                        throw new IllegalStateException("Cannot add null.");
                    }
                    consumer.accept(currentItem);
                    break;
                case TIME_NAME:
                    currentItem.setTime(Long.valueOf(content));
                    LOG.debug(MessageFormat.format("parsing time: string [{0}], int[{1}]", content, currentItem.getTime()));
                    break;
                case AMOUNT_NAME:
                    currentItem.setAmount(new BigDecimal(content));
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            content = String.copyValueOf(ch, start, length).trim();
        }
    }
}