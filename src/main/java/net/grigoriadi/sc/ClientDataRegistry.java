package net.grigoriadi.sc;

import java.text.MessageFormat;
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
     * We need to know which clients are active, in order to sorting data correctly.
     * @param clientId unique client id with active state
     */
    public void registerClient(String clientId) {
        System.out.println(MessageFormat.format("REGISTERING CLIENT ID: {0}", clientId));
        registeredClients.add(clientId);
    }

    public void deregisterClient(String clientId) {
        registeredClients.remove(clientId);
    }

    public boolean allClientsShutDown() {
        return registeredClients.size() == 0;
    }

    /**
     * Each client puts entry here after each item parsed from stream.
     * Given the contract that data are ordered in each stream, we can than calculate
     * if next candidate is safe to pe polled from queue.
     * {@see ClientDataRegistry#allClientsAhead}
     */
    public void registerLastClientTime(String clientId, Long time) {
        lastItemTimesByClient.put(clientId, time);
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
            for (String clientId : registeredClients) {
                Long time = lastItemTimesByClient.get(clientId);
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
