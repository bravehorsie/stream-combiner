package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.sc.domain.Client;
import net.grigoriadi.sc.processing.IStreamParser;
import net.grigoriadi.sc.processing.ItemHandler;
import net.grigoriadi.sc.processing.JAXBParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * A client connecting to provided host and port.
 */
public class StreamClientTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StreamClientTask.class);

    //TODO externalize host port and id as ClientId with hashcode
    private final String host;

    private final int port;

    private final String clientId;

    private final IStreamParser streamParser;

    public StreamClientTask(String host, int port, Integer id) {
        this.host = host;
        this.port = port;
        this.clientId = host + ":" + port + "-" + id;
        this.streamParser = new JAXBParser(new ItemHandler(clientId));
        AppContext.getInstance().getClientRegistry().registerClient(new Client(clientId, true));
    }

    @Override
    public void run() {
        InputStream clientInputStream = null;
        try {
            Socket client = new Socket(host, port);
            clientInputStream = client.getInputStream();
            streamParser.readStream(clientInputStream);
            clientInputStream.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LOG.debug("Exiting client: " + clientId);
            AppContext.getInstance().getClientRegistry().registerLastClientTime(clientId, Long.MAX_VALUE);
            AppContext.getInstance().getClientRegistry().registerClient(new Client(clientId, false));
            if (clientInputStream != null) {
                try {
                    clientInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
