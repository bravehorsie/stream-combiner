package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.sc.domain.Client;
import net.grigoriadi.sc.processing.XmlDataBindingFactory;
import net.grigoriadi.sc.processing.parsing.IStreamParser;
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

    private final String host;

    private final int port;

    private final String clientId;

    private final IStreamParser streamParser;

    public StreamClientTask(String host, int port, Integer id) {
        this.host = host;
        this.port = port;
        this.clientId = host + ":" + port + "-" + id;
        this.streamParser = XmlDataBindingFactory.newStreamParser(clientId);
        AppContext.getInstance().getClientRegistry().registerClient(new Client(clientId, true));
        LOG.debug("CREATED CLIENT "+clientId);
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
