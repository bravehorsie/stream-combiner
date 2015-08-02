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
        if (itemWrittenListener != null) {
            itemWrittenListener.itemWritten(time, amount);
        }
    }

    protected BigDecimal newAmount() {
        Random r = new Random();
        int i = r.nextInt(11);
        if (r.nextInt(11) % 10 == 0) {
            i = i * -1;
        }
        return new BigDecimal(i * 10);
    }

    /**
     * Wait a bit for timing desynchronization.
     * Simulates that streams for clients go occasionally ahead of each other
     */
    protected void desynchronizeTiming() {
        randomWait(100, 10);
    }

    protected void simulateOccasionalServerHang() {
        //0.0001% chance to hang a server
        randomWait(100000, 1000);
    }

    /**
     * Occasionally sleeps for a random period of time.
     * @param probability the higher number the lesser chance (for probability 100 there is a 1% chance to proc)
     * @param maxSleepTime maximal bound of a chosen sleep in milliseconds
     */
    private void randomWait(int probability, int maxSleepTime) {
        Random random = new Random();
        if (random.nextInt(probability + 1) % probability == 0) {
            try {
                Thread.sleep(random.nextInt(maxSleepTime));
            } catch (InterruptedException e) {
                //called from runnable
                Thread.currentThread().interrupt();
            }
        }

    }
}
