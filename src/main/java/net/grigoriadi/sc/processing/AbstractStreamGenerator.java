package net.grigoriadi.sc.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Abstract class for stream data generation.
 */
public abstract class AbstractStreamGenerator implements IStreamGenerator {

    private static Logger LOG = LoggerFactory.getLogger(AbstractStreamGenerator.class);

    private final IItemWrittenListener itemWrittenListener;

    public AbstractStreamGenerator() {
        itemWrittenListener = null;
    }

    public AbstractStreamGenerator(IItemWrittenListener itemWrittenListener) {
        this.itemWrittenListener = itemWrittenListener;
    }

    protected void onItemWritten(Long time, BigDecimal amount) {
        LOG.debug("Generated item time: {0}, amount:{1}", time, amount);
        if (itemWrittenListener != null) {
            itemWrittenListener.itemWritten(time, amount);
        }
    }

    /**
     * Wait a bit for timing desynchronization.
     * @param itemCount row count of a processed item
     */
    protected void randomWait(long itemCount) {
        if (itemCount % 100 == 0) {
            try {
                Thread.sleep(new Random().nextInt(10));
            } catch (InterruptedException e) {
                //called from runnable
                Thread.currentThread().interrupt();
            }
        }
    }
}
