package net.grigoriadi.sc;

import net.grigoriadi.sc.domain.Client;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Register of all connected client sockets and last items they have processed.
 */
public class ClientDataRegistry {

    private final CopyOnWriteArraySet<Client> registeredClients = new CopyOnWriteArraySet<>();
    private final Map<String, Long> lastItemTimesByClient = new ConcurrentHashMap<>();
    private volatile Runnable allClientsShutDownListener;

    private final Object lock = new Object();

    ClientDataRegistry() {

    }

    /**
     * We need to know which clients are registered, in order to sorting data correctly.
     *
     * @param client unique client id
     */
    public void registerClient(Client client) {
        System.out.println(MessageFormat.format("REIGSTERING CLIENT ID: {0}, ACTIVE: {1}", client.getClientId(), client.isActive() ));
        synchronized (lock) {
            registeredClients.remove(client);
            registeredClients.add(client);
        }
        if (allClientsShutDown()) {
            allClientsShutDownListener.run();
        }
    }

    public void registerLastClientTime(String clientId, Long time) {
        lastItemTimesByClient.put(clientId, time);
    }

    public boolean allClientsShutDown() {
        for (Client client : registeredClients) {
            if (client.isActive()) {
                return false;
            }
        }
        return true;
    }

    //TODO doc
    public boolean allClientsAhead(Long workQueueHeadTime) {
        synchronized (lock) {
            for (Client client : registeredClients) {
                Long time = lastItemTimesByClient.get(client.getClientId());
                //client hasn't parsed any items yet, so time is null
                //workQueueHeadTime is ahead of any registered client last workQueueHeadTime
                if (time == null || workQueueHeadTime > time) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * Effectively used once by one single thread.
     */
    public void setAllClientsShutDownListener(Runnable allClientsShutDownListener) {
        this.allClientsShutDownListener = allClientsShutDownListener;
    }

}
