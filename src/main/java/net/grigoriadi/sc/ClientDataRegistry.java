package net.grigoriadi.sc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Register of all connected client sockets and last items they have processed.
 */
public class ClientDataRegistry {

    private final CopyOnWriteArraySet<String> registeredClients = new CopyOnWriteArraySet<>();
    private final Map<String, Long> lastItemTimesByClient = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    ClientDataRegistry() {

    }

    /**
     * We need to know which clients are registered, in order to sorting data correctly.
     *
     * @param clientName unique client id
     */
    public void registerClient(String clientName) {
        synchronized (lock) {
            if (registeredClients.contains(clientName)) {
                throw new IllegalStateException("Client already registered!");
            }
            registeredClients.add(clientName);
        }
    }


    public void addLast(String clientName, Long time) {
        lastItemTimesByClient.put(clientName, time);
    }

    //TODO doc
    public boolean allClientsAhead(Long workQueueHeadTime) {
        synchronized (lock) {
            for (String client : registeredClients) {
                Long time = lastItemTimesByClient.get(client);
                //client hasn't parsed any items yet, so time is null
                //workQueueHeadTime is ahead of any registered client last workQueueHeadTime
                if (time == null || workQueueHeadTime > time) {
                    return false;
                }
            }

        }
        return true;
    }

    public void registerLastClientTime(String clientName, Long time) {
        lastItemTimesByClient.put(clientName, time);
    }

}
