package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppConetxt;
import net.grigoriadi.sc.domain.Item;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A worker polling items from an priority queue, and putting them for marshalling.
 * This worker is designed as a single thread worker only.
 */
public class QueueWorker implements Runnable {

    private IDataMarshaller marshaller;

    private long itemCount;

    private BigDecimal totalReceivedAmount = BigDecimal.ZERO;

    public QueueWorker() {
        this.marshaller = new JsonDataMarshaller();
    }

    @Override
    public void run() {
        Item lastItem = null;
        //TODO correct interruptin of worker
        while (true) {
            try {
                Item item = waitForItem();

                if (lastItem != null && item.getTime().compareTo(lastItem.getTime()) < 0) {
                    throw new IllegalStateException(MessageFormat.format("Items in wrong order item {0}, last {1}.", item.getTime(), lastItem.getTime()));
                }

                pushData(item);
                lastItem = item;
            } catch (InterruptedException e) {
                System.out.println("INTERRUPTED!!");
                Thread.currentThread().interrupt();
                break;
            }

        }
    }

    private void pushData(Item item) {
        marshaller.marshallData(item);
        totalReceivedAmount = totalReceivedAmount.add(item.getAmount());
        System.out.println("total amount: "+totalReceivedAmount + " polled count "+itemCount);
    }

    private Item waitForItem() throws InterruptedException {
        PriorityBlockingQueue<Long> workQueue = AppConetxt.getInstance().getTimeQueue();
        Long itemTime;
        while (((itemTime = workQueue.peek()) == null) || !AppConetxt.getInstance().getClientRegistry().allClientsAhead(itemTime)) {

            /* TODO probably not needed at all
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }*/
            Thread.sleep(10);
        }
        itemTime = workQueue.poll();
        itemCount++;
        return AppConetxt.getInstance().getItems().remove(itemTime);
    }

}
