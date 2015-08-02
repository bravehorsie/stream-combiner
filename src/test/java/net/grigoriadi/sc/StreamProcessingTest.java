package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.sc.processing.generation.IItemWrittenListener;
import net.grigoriadi.sc.processing.generation.IStreamGenerator;
import net.grigoriadi.sc.processing.generation.JaxbStreamGenerator;
import net.grigoriadi.sc.processing.generation.StaxStreamGenerator;
import net.grigoriadi.sc.processing.parsing.IStreamParser;
import net.grigoriadi.sc.processing.parsing.JAXBParser;
import net.grigoriadi.sc.processing.parsing.SaxParser;
import net.grigoriadi.sc.processing.parsing.StaxParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Tests data processing
 */
public class StreamProcessingTest {

    private static class OutputAmountCounter implements IItemWrittenListener {

        private BigDecimal amountSum = BigDecimal.ZERO;

        @Override
        public void itemWritten(Long time, BigDecimal amount) {
            this.amountSum = this.amountSum.add(amount);
        }

        public BigDecimal getAmountSum() {
            return amountSum;
        }
    }

    private static class InputAmountCounter implements Consumer<Item> {
        private BigDecimal amountSum = BigDecimal.ZERO;

        public void accept(Item item) {
            this.amountSum = this.amountSum.add(item.getAmount());
        }

        public BigDecimal getAmountSum() {
            return amountSum;
        }
    }

    @Test
    public void testJaxbProcessing() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        JaxbStreamGenerator generator = new JaxbStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        JAXBParser parser = new JAXBParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    @Test
    public void testStaxProcessing() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        StaxStreamGenerator generator = new StaxStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        StaxParser parser = new StaxParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    @Test
    public void testStaxToSax() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        StaxStreamGenerator generator = new StaxStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        SaxParser parser = new SaxParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    @Test
    public void testJaxbToStax() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        JaxbStreamGenerator generator = new JaxbStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        SaxParser parser = new SaxParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    @Test
    public void testJaxbToSax() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        JaxbStreamGenerator generator = new JaxbStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        SaxParser parser = new SaxParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    @Test
    public void testStaxToJaxb() throws IOException, InterruptedException {
        OutputAmountCounter itemWrittenListener = new OutputAmountCounter();
        StaxStreamGenerator generator = new StaxStreamGenerator(itemWrittenListener);
        InputAmountCounter itemReadListener = new InputAmountCounter();
        JAXBParser parser = new JAXBParser(itemReadListener);

        processStreams(generator, parser);

        assertSumsEqual(itemWrittenListener, itemReadListener);
    }

    private void assertSumsEqual(OutputAmountCounter itemWrittenListener, InputAmountCounter itemReadListener) {
        Assert.assertTrue(itemReadListener.getAmountSum().longValue() > 0 && itemWrittenListener.getAmountSum().longValue() > 0);
        System.out.println(MessageFormat.format("Amount sum read: [{0}], amount sum written: [{1}]", itemReadListener.getAmountSum(), itemWrittenListener.getAmountSum()));
        Assert.assertEquals(itemReadListener.getAmountSum(), itemWrittenListener.getAmountSum());
    }

    private void processStreams(IStreamGenerator generator, IStreamParser parser) throws IOException, InterruptedException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        Thread threadOut = new Thread(() -> generator.writeStream(out));
        threadOut.start();

        Thread threadIn = new Thread(() -> parser.readStream(in));
        threadIn.start();

        threadOut.join();
        threadIn.join();
    }
}
