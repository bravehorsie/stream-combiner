package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Context of an application.
 */
public class AppConetxt {

    private static final AppConetxt instance;

    private final PriorityBlockingQueue<Long> timeQueue = new PriorityBlockingQueue<>();

    private final ConcurrentHashMap<Long, Item> items = new ConcurrentHashMap<>();

    private final ClientDataRegistry clientRegistry = new ClientDataRegistry();

    static {
        instance = new AppConetxt();
    }

    private AppConetxt() {

    }

    public static AppConetxt getInstance() {
        return instance;
    }

    public PriorityBlockingQueue<Long> getTimeQueue() {
        return timeQueue;
    }

    public ClientDataRegistry getClientRegistry() {
        return clientRegistry;
    }

    public ConcurrentHashMap<Long, Item> getItems() {
        return items;
    }
}


