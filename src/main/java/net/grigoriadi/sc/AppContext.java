package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Context of an application.
 */
public class AppContext {

    //Constant of how many items should be generated for each client.
    public static final long GENERATED_ITEM_COUNT_PER_CONNECTION = 1000L;

    private static final AppContext instance;

    private final PriorityBlockingQueue<Long> workQueue = new PriorityBlockingQueue<>();

    private final ConcurrentHashMap<Long, Item> itemSums = new ConcurrentHashMap<>();

    private final ClientDataRegistry clientRegistry = new ClientDataRegistry();

    private BigDecimal totalGeneratedAmount = BigDecimal.ZERO;

    private final Object lock = new Object();

    static {
        instance = new AppContext();
    }

    private AppContext() {

    }

    public static AppContext getInstance() {
        return instance;
    }

    /**
     * A queue of received times (represented with long) by clients.
     * @return
     */
    public PriorityBlockingQueue<Long> getWorkQueue() {
        return workQueue;
    }

    public ClientDataRegistry getClientRegistry() {
        return clientRegistry;
    }

    public ConcurrentHashMap<Long, Item> getItemSums() {
        return itemSums;
    }

    /**
     * Gets a total amount generated by server threads.
     * Thread safe.
     * @return total amount.
     */
    public BigDecimal getTotalGeneratedAmount() {
        synchronized (lock) {
            return totalGeneratedAmount;
        }
    }

    /**
     * Adds an amount to total sum of generated data.
     * @param amount amount to add
     */
    public void addToTotalGeneratedAmount(BigDecimal amount) {
        synchronized (lock) {
            this.totalGeneratedAmount = totalGeneratedAmount.add(amount);
        }
    }
}


