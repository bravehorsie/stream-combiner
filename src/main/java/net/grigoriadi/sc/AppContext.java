package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Context of an application.
 */
public class AppContext {

    private static Logger LOG = LoggerFactory.getLogger(AppContext.class);

    private static final String GENERATED_ITEM_COUNT_PER_CONNECTION = "GENERATED_ITEM_COUNT_PER_CONNECTION";

    private Properties properties;

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
     * This belongs to tests actually, but given a character of an app, I considered better testing itself also while running.
     * @param amount amount to add
     */
    public void addToTotalGeneratedAmount(BigDecimal amount) {
        synchronized (lock) {
            this.totalGeneratedAmount = totalGeneratedAmount.add(amount);
        }
    }

    /**
     * How many items should be generated per client connection.
     * Default is 100k items, therefore if ran with 10 clients, 1m items will be generated.
     * Change properties (or test properties) file for more.
     */
    public long getGeneratedItemCountPerConnection() {
        return Long.parseLong(getProperties().getProperty(GENERATED_ITEM_COUNT_PER_CONNECTION));
    }

    public Properties getProperties() {
        synchronized (lock) {
            if (properties == null) {
                properties = new Properties();
                try {
                    properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
                } catch (IOException e) {
                    LOG.error("Error reading properties", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return properties;
    }
}


