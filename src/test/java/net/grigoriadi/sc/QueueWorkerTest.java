package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Client;
import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.sc.processing.ItemHandler;
import net.grigoriadi.sc.processing.JsonDataMarshaller;
import net.grigoriadi.sc.processing.QueueWorker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Tests a queue worker to receive data.
 */
public class QueueWorkerTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    private static class DataGeneratorTask implements Runnable {

        private long lastTime = 0;

        private final String id = UUID.randomUUID().toString();

        private Consumer<Item> itemConsumer;

        public DataGeneratorTask() {
            itemConsumer = new ItemHandler(id);
            AppContext.getInstance().getClientRegistry().registerClient(new Client(id, true));
        }

        @Override
        public void run() {
            AppContext appContext = AppContext.getInstance();
            for (int i = 0; i < AppContext.getInstance().getGeneratedItemCountPerConnection(); i++) {
                Random r = new Random();
                lastTime += r.nextInt(3);
                BigDecimal amount = new BigDecimal(r.nextInt(101));
                Item item = new Item(lastTime, amount);
                itemConsumer.accept(item);
                appContext.getClientRegistry().registerLastClientTime(id, lastTime);
                appContext.addToTotalGeneratedAmount(amount);
            }
            appContext.getClientRegistry().registerLastClientTime(id, Long.MAX_VALUE);
            appContext.getClientRegistry().registerClient(new Client(id, false));
            System.out.println(MessageFormat.format("Client id {0} exited successfully.", id));
        }
    }

    private static class SumCounterDataMarshaller extends JsonDataMarshaller {

        private BigDecimal sum = BigDecimal.ZERO;

        @Override
        public void marshallData(Item data) {
            synchronized (this) {
                sum = sum.add(data.getAmount());
            }
            super.marshallData(data);
        }

        public synchronized BigDecimal getSum() {
            return sum;
        }
    }

    @Before
    public void before() {
    }

    @Test
    public void testReceiveData() {

        SumCounterDataMarshaller sumAppender = new SumCounterDataMarshaller();
        QueueWorker worker = new QueueWorker(sumAppender);
        Thread workerThread = new Thread(worker);

        for (int i = 0; i < 5; i++) {
            executorService.execute(new DataGeneratorTask());
        }

        workerThread.start();

        try {
            Thread.sleep(1000);
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
            System.out.println("exe service awaited");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            workerThread.join(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Sum generated does not match sum received", sumAppender.getSum(), AppContext.getInstance().getTotalGeneratedAmount());
        Assert.assertFalse("Worker didn't exit successfully", workerThread.isAlive());
    }
}
