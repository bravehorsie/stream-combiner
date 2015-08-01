package net.grigoriadi.sc;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class QueueTest {

    private static class QueueGenerator implements Runnable {
        private PriorityBlockingQueue<Integer> queue;
        private String name;

        public QueueGenerator(PriorityBlockingQueue<Integer> queue, String name) {
            this.queue = queue;
            this.name = name;
        }

        @Override
        public void run() {
            Random random = new Random();
            int last = 0;
            while (true) {
                try {
                    //TODO latch
                    Thread.sleep(random.nextInt(300));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                //TODO latch
                last = last + random.nextInt(5);
                queue.add(last);
                System.out.println(name + " generated value: " + last);
            }
        }
    }

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));

    @Test
    public void testQueue() throws InterruptedException {
        final PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();
        executor.execute(new QueueGenerator(queue, "G1"));
        executor.execute(new QueueGenerator(queue, "G2"));
        executor.execute(() -> {
            while (true) {
                try {
                    //TODO latch
                    Thread.sleep(50);
                    System.out.println("polling: " + queue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread.sleep(10000L);
    }

}
