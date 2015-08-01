package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppConetxt;
import net.grigoriadi.sc.domain.Item;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A worker polling items from an priority queue, and putting them for marshalling.
 * This worker is designed as a single thread worker only.
 */
public class QueueWorker implements Runnable {

    private IDataMarshaller marshaller;

    private long itemCount;

    public QueueWorker() {
        this.marshaller = new JsonDataMarshaller();
    }

    @Override
    public void run() {
        Item itemSum = null;
        //TODO queue worker never interrupted.
        while (true) {
            try {
                Item nextItem = takeItem();

                /*if (itemSum == null) {
                    itemSum = nextItem;
                    continue;
                }

                while (nextItem.getTime().equals(itemSum.getTime())) {
                    itemSum.setAmount(itemSum.getAmount().add(nextItem.getAmount()));
                    nextItem = takeItem();
                }*/

                if (itemSum != null && nextItem.getTime().compareTo(itemSum.getTime()) < 0) {
                    throw new IllegalStateException(MessageFormat.format("Items in wrong order nextItem {0}, last {1}.", nextItem.getTime(), itemSum.getTime()));
                }

                marshaller.marshallData(nextItem);
                itemSum = nextItem;
                System.out.println(itemCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }

    private Item takeItem() throws InterruptedException {
        PriorityBlockingQueue<Item> workQueue = AppConetxt.getInstance().getTimeQueue();
        Item item;
        while ((item = workQueue.peek()) == null || !allClientsAhead(item)) {
            Thread.sleep(10);
        }
        item = workQueue.take();
        itemCount++;
        return item;
    }

    //TODO doc
    private boolean allClientsAhead(Item item) {
        Map<String, Long> registry = AppConetxt.getInstance().getLastTimes();
        if (registry.size() < AppConetxt.getInstance().getClientCount()) {
            return false;
        }
        for (Map.Entry<String, Long> entry : registry.entrySet()) {
            Long value = entry.getValue();
            if (item.getTime().compareTo(value) >= 0) {
                System.out.println(MessageFormat.format("LastEntry: {0}, Item: {1}", value, item.getTime()));
                return false;
            }
        }
        return true;
    }
}
