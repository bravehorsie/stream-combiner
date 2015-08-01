package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppConetxt;
import net.grigoriadi.sc.ClientDataRegistry;
import net.grigoriadi.sc.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A worker polling items from an priority queue, and putting them for marshalling.
 * This worker is designed as a single thread worker only.
 */
public class QueueWorker implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(QueueWorker.class);

    private IDataMarshaller marshaller;

    private long itemCount;

    private BigDecimal totalReceivedAmount = BigDecimal.ZERO;

    public QueueWorker() {
        this.marshaller = new JsonDataMarshaller();
    }

    @Override
    public void run() {
        Item lastItem = null;
        PriorityBlockingQueue<Long> workQueue = AppConetxt.getInstance().getTimeQueue();
        ClientDataRegistry clientRegistry = AppConetxt.getInstance().getClientRegistry();
        while (workQueue.size() > 0 || !clientRegistry.allClientsShutDown()) {
            try {
                Long itemTime;
                while (((itemTime = workQueue.peek()) == null) || !clientRegistry.allClientsAhead(itemTime)) {
                    //wait for an item to become available, note that if queue is not empty
                    //it doesn't simply mean next head item should be taken.
                    Thread.sleep(10);
                    System.out.println("SLEEP");
                }
                itemTime = workQueue.take();
                itemCount++;
                Item item = AppConetxt.getInstance().getItems().remove(itemTime);

                if (lastItem != null && item.getTime().compareTo(lastItem.getTime()) < 0) {
                    throw new IllegalStateException(MessageFormat.format("Items in wrong order item {0}, last {1}.", item.getTime(), lastItem.getTime()));
                }

                pushData(item);
                lastItem = item;
            } catch (InterruptedException e) {
                //Continue on main loop receiving interrupt.
                //It is not necessary to interrupt from outside, when all clients are down, cycle will end anyway.
                //Still this thread is interrupted when all clients are shut down, to stay formally correct.
                LOG.info(MessageFormat.format("Received \"all clients down event\" from app, allClientsShutDown: {0}, workQueueSize: {1}", clientRegistry.allClientsShutDown(), workQueue.size()));
            }
        }
        LOG.info("Queue worker exited successfully.");
    }


    private void pushData(Item item) {
        marshaller.marshallData(item);
        totalReceivedAmount = totalReceivedAmount.add(item.getAmount());
        System.out.println("total amount: " + totalReceivedAmount + " polled count " + itemCount);
    }


}
