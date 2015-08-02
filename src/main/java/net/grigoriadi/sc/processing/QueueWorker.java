package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppContext;
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

    private BigDecimal totalReceivedAmount = BigDecimal.ZERO;

    public QueueWorker() {
        this.marshaller = new JsonDataMarshaller();
    }

    @Override
    public void run() {
        Item lastItem = null;
        AppContext context = AppContext.getInstance();
        PriorityBlockingQueue<Long> workQueue = context.getTimeQueue();
        ClientDataRegistry clientRegistry = context.getClientRegistry();
        while (workQueue.size() > 0 || !clientRegistry.allClientsShutDown()) {
            try {
                Long itemTime;
                while (((itemTime = workQueue.peek()) == null) || !clientRegistry.allClientsAhead(itemTime)) {
                    //wait for an item to become available, note that if queue is not empty
                    //it doesn't simply mean next head item should be taken.
                    LOG.debug("Clients are too slow, waiting for them.");
                    Thread.sleep(5);
                }

                itemTime = workQueue.take();
                Item item = context.getItems().remove(itemTime);
                if (lastItem != null && item.getTime().compareTo(lastItem.getTime()) < 0) {
                    throw new IllegalStateException(MessageFormat.format("Items in wrong order item {0}, last {1}.", item.getTime(), lastItem.getTime()));
                }

                marshaller.marshallData(item);
                totalReceivedAmount = totalReceivedAmount.add(item.getAmount());
                lastItem = item;
            } catch (InterruptedException e) {
                //Continue on main loop receiving interrupt.
                //It is not necessary to interrupt from outside, when all clients are down, cycle will end anyway.
                //Still this thread is interrupted when all clients are shut down, to stay formally correct.
                LOG.info(MessageFormat.format("Received \"all clients down event\" from app, allClientsShutDown: {0}, workQueueSize: {1}", clientRegistry.allClientsShutDown(), workQueue.size()));
            }
        }
        LOG.info("Queue worker exited successfully.");
        LOG.info(MessageFormat.format("=========================== Total generated amount: [{0}], total received amount: [{1}] ===========================", context.getTotalGeneratedAmount(), totalReceivedAmount));
        if (!context.getTotalGeneratedAmount().equals(totalReceivedAmount)) {
            LOG.error("Sum of amounts received is not equal sum of amounts sent!! This should not happen even if manually interrupted!");
            throw new IllegalStateException("Amount sum crash!");
        }
    }




}
