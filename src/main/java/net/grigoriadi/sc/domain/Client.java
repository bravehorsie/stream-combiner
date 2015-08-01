package net.grigoriadi.sc.domain;

/**
 * Identification of a client based on:
 * clientId: concatenated host:port-id
 * active: status whether client is receiving data on socket.
 *
 * Immutable
 */
public class Client {

    private final String clientId;

    private final boolean isActive;

    public Client(String clientId, boolean isActive) {
        this.clientId = clientId;
        this.isActive = isActive;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return clientId.equals(client.clientId);

    }

    @Override
    public int hashCode() {
        return clientId.hashCode();
    }
}
