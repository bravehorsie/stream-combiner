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
 * A worker polling candidates from an priority queue, and putting them on for marshalling.
 * This worker is designed as a single consumer worker only.
 *
 * Given the requirement that data on output stream are ordered, rule for candidate being available to poll from a queue is:
 * All running clients parsing data, "are ahead" of the head candidate in the queue.
 *
 * Eg. if head candidate time is 10, worker is waiting for all running clients to have received at least time 11.
 * Only after reaching this state worker polls head candidate.
 * (Data stream per client is ordered by contract).
 * @see ClientDataRegistry#allClientsAhead(Long)
 * When client is done processing stream it sets Long.MAX_VALUE as its last received date, so the queue can be drained to tail.
 *
 * Time instants in queue are "distinct", they not duplicate each other.
 * Sum amount for each item is held in "Item Sum" map in app context. Where sums are pushed by clients.
 */
public class QueueWorker implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(QueueWorker.class);

    private static final long SLEEP_TIME = 5L;

    private static final long THREE_MINUTES_TIMEOUT = 1000 * 60 * 3;

    private IDataMarshaller marshaller;

    private BigDecimal totalReceivedAmount = BigDecimal.ZERO;

    public QueueWorker() {
        this.marshaller = new JsonDataMarshaller();
    }

    public QueueWorker(IDataMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public void run() {
        Item lastItem = null;
        AppContext context = AppContext.getInstance();
        PriorityBlockingQueue<Long> workQueue = context.getWorkQueue();
        ClientDataRegistry clientRegistry = context.getClientRegistry();
        while (workQueue.size() > 0 || !clientRegistry.allClientsShutDown()) {
            try {
                Long itemTime;
                Long totalWaitingTime = 0L;
                while (((itemTime = workQueue.peek()) == null) || !clientRegistry.allClientsAhead(itemTime)) {
                    //wait for an item to become available, note that if queue is not empty
                    //it doesn't simply mean next head item should be taken.
                    //As there is a server hang simulation (see AbstractStreamGenerator#simulateOccasionalServerHang)
                    //next line should appear in log plentifully
                    LOG.debug("Clients are too slow, waiting for them.");
                    Thread.sleep(SLEEP_TIME);
                    totalWaitingTime += SLEEP_TIME;
                    if (totalWaitingTime >= THREE_MINUTES_TIMEOUT) {
                        //prevent OOM in case one or more clients are hanging
                        LOG.error("One or more clients hanging too long, exiting worker thread.");
                        return;
                    }
                }

                //given above while waiting condition, we should actually never get blocked on take().
                itemTime = workQueue.take();
                Item item = context.getSummedItems().remove(itemTime);
                //Self integrity test. This belongs to junit, but given the character of the app, it doesn't hurt while running main
                if (lastItem != null && item.getTime().compareTo(lastItem.getTime()) < 0) {
                    throw new IllegalStateException(MessageFormat.format("Items in wrong order item {0}, last {1}.", item.getTime(), lastItem.getTime()));
                }

                marshaller.marshallData(item);
                totalReceivedAmount = totalReceivedAmount.add(item.getAmount());
                lastItem = item;
            } catch (InterruptedException e) {
                //Since main loop is driven by the fact, that there are o more active producers of the queue,
                //and take() should never block, interrupt from outside is not expected and would make no sense.
                //Continue on main loop receiving interrupt anyway.
                LOG.info(MessageFormat.format("Not expected to receive interrupt here, allClientsShutDown: {0}, workQueueSize: {1}", clientRegistry.allClientsShutDown(), workQueue.size()));
            }
        }
        LOG.info("Queue worker exited successfully.");
        LOG.info(MessageFormat.format("=========================== Total generated amount: [{0}], total received amount: [{1}] ===========================", context.getTotalGeneratedAmount(), totalReceivedAmount));
        //Self integrity test. This belongs to junit, but given the character of the app, it doesn't hurt while running main
        if (!context.getTotalGeneratedAmount().equals(totalReceivedAmount)) {
            LOG.error("Sum of amounts received is not equal sum of amounts sent!! This should not happen even if manually interrupted!");
            throw new IllegalStateException("Amount sum crash!");
        }
    }
}
