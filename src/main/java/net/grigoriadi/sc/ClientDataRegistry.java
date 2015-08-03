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

    private final Object lock = new Object();

    ClientDataRegistry() {

    }

    /**
     * We need to know which clients are registered, in order to sorting data correctly.
     *
     * @param client unique client id
     */
    public void registerClient(Client client) {
        System.out.println(MessageFormat.format("REGISTERING CLIENT ID: {0}, ACTIVE: {1}", client.getClientId(), client.isActive() ));
        synchronized (lock) {
            registeredClients.remove(client);
            registeredClients.add(client);
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

    /**
     * All clients have parsed data with times past given time.
     *
     * Eg. if provided workQueueHeadTime time is 10, all clients must have received at least time 11 to return true.
     * (Data stream per client are ordered by contract).
     *
     * @param workQueueHeadTime time to check against all clients
     * @return true if all clients have received data with time after provided time in argument
     */
    public boolean allClientsAhead(Long workQueueHeadTime) {
        synchronized (lock) {
            for (Client client : registeredClients) {
                Long time = lastItemTimesByClient.get(client.getClientId());
                //client hasn't parsed any items yet, so time is null
                //workQueueHeadTime is ahead of any registered client last workQueueHeadTime
                if (time == null || workQueueHeadTime >= time) {
                    return false;
                }
            }

        }
        return true;
    }

}
