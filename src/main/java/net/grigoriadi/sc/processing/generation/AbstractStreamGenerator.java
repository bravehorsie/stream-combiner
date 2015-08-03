package net.grigoriadi.sc.processing.generation;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Abstract class for stream data generation.
 */
public abstract class AbstractStreamGenerator implements IStreamGenerator {

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
        Random r = ThreadLocalRandom.current();
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
        //0.001% chance to hang a server for a short period on writing of each item.
        randomWait(100000, 1000);
    }

    /**
     * Occasionally sleeps for a random period of time.
     * @param probability the higher number, the lesser chance (for probability 100 there is a 1% chance to proc)
     * @param maxSleepTime maximal bound of a chosen sleep in milliseconds
     */
    private void randomWait(int probability, int maxSleepTime) {
        Random random = ThreadLocalRandom.current();
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
