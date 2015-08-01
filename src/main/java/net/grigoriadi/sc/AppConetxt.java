package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Context of an application.
 */
public class AppConetxt {

    private static final AppConetxt instance;

    private volatile int clientCount;

    private final ConcurrentHashMap<Long, BigDecimal> amounts = new ConcurrentHashMap<>();

    private final PriorityBlockingQueue<Item> timeQueue = new PriorityBlockingQueue<>();

    private final Map<String, Long> lastTimes = new ConcurrentHashMap<>();

    static {
        instance = new AppConetxt();
    }

    private AppConetxt() {

    }

    public static AppConetxt getInstance() {
        return instance;
    }

    public int getClientCount() {
        return clientCount;
    }

    synchronized void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public synchronized void decreaseClientCount() {
        clientCount--;
    }

    public ConcurrentHashMap<Long, BigDecimal> getAmounts() {
        return amounts;
    }

    public PriorityBlockingQueue<Item> getTimeQueue() {
        return timeQueue;
    }

    synchronized public Map<String, Long> getLastTimes() {
        return lastTimes;
    }
}


