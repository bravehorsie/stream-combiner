package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.sc.processing.IDataMarshaller;
import net.grigoriadi.sc.processing.JsonDataMarshaller;
import net.grigoriadi.sc.processing.QueueWorker;
import net.grigoriadi.sc.processing.parsing.ItemHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
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

        private Runnable hangingSimulator;

        public DataGeneratorTask() {
            itemConsumer = new ItemHandler(id);
            AppContext.getInstance().getClientRegistry().registerClient(id);
        }

        public DataGeneratorTask(Runnable hangingSimulator) {
            this();
            this.hangingSimulator = hangingSimulator;
        }

        @Override
        public void run() {
            AppContext appContext = AppContext.getInstance();
            for (int i = 0; i < AppContext.getInstance().getGeneratedItemCountPerConnection(); i++) {
                if (hangingSimulator != null) {
                    hangingSimulator.run();
                }
                lastTime += ThreadLocalRandom.current().nextInt(100);
                BigDecimal amount = new BigDecimal(ThreadLocalRandom.current().nextInt(100));
                Item item = new Item(lastTime, amount);
                itemConsumer.accept(item);
                appContext.getClientRegistry().registerLastClientTime(id, lastTime);
                appContext.addToTotalGeneratedAmount(amount);
            }
            appContext.getClientRegistry().deregisterClient(id);
            System.out.println(MessageFormat.format("Client id {0} exited successfully.", id));
        }
    }


    /**
     * Effectively single threaded singleton.
     * Checks, that items comes ordered, and counts total sum of items processed,
     * so test can assert it with total sum generated.
     */
    private static class DecorateCheckOrderAndCountSumMarshaller implements IDataMarshaller {

        private BigDecimal sum = BigDecimal.ZERO;

        private Long lastReceivedTime = -1L;

        private IDataMarshaller delegate = new JsonDataMarshaller();

        @Override
        public void marshallData(Item data) {
            synchronized (this) {
                sum = sum.add(data.getAmount());
                //Asserts order of items.
                Assert.assertTrue(MessageFormat.format("Wrong item order: Last received time {0}, item time {1}", lastReceivedTime, data.getTime()), lastReceivedTime < data.getTime());
                lastReceivedTime = data.getTime();
            }
            delegate.marshallData(data);
        }

        public synchronized BigDecimal getSum() {
            return sum;
        }
    }

    @Before
    public void before() {
    }

    /**
     * Testing threads thoroughly seems to be a lot of fun, I am not sure yet how to properly do that.
     * This test tests priority order and total sum received is equal to total sum sent.
     * It also simulates clients random hanging.
     */
    @Test
    public void testReceiveData() {

        DecorateCheckOrderAndCountSumMarshaller sumAppender = new DecorateCheckOrderAndCountSumMarshaller();
        QueueWorker worker = new QueueWorker(sumAppender);
        Thread workerThread = new Thread(worker);

        //run 20 clients, 10.000 items will be generated per client as defined in src/test/properties/app.properties
        for (int i = 0; i < 20; i++) {
            Runnable clientHangSimulator = null;
            if (i % 5 == 0) {
                //simulate occasional client hang for four of the total twenty clients and check worker is not biased.
                clientHangSimulator = () -> {
                    if (ThreadLocalRandom.current().nextInt(1001) % 1000 == 0) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                };
            }
            executorService.execute(new DataGeneratorTask(clientHangSimulator));
        }

        workerThread.start();

        try {
            Thread.sleep(1000);
            executorService.shutdown();
            //runs actually much faster, may need update if item count per client is drastically increased in app.properties
            executorService.awaitTermination(2 * 60, TimeUnit.SECONDS);
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
