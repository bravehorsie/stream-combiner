package net.grigoriadi.sc;

import net.grigoriadi.sc.processing.JaxbStreamGenerator;
import org.junit.Test;

/**
 * Tests data processing
 */
public class StreamProcessingTest {

    @Test
    public void testJaxbGenerator() {
        JaxbStreamGenerator generator = new JaxbStreamGenerator();

        try {
            generator.writeStream(System.out);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
