package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.AppContext;
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
        AppContext.getInstance().getClientRegistry().registerClient(clientId);
        LOG.debug("CREATED CLIENT " + clientId);
    }

    @Override
    public void run() {
        InputStream clientInputStream = null;
        Socket client = null;
        try {
            client = new Socket(host, port);
            clientInputStream = client.getInputStream();
            streamParser.readStream(clientInputStream);
            clientInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientInputStream != null) {
                    clientInputStream.close();
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOG.debug("Exiting client: " + clientId);
            AppContext.getInstance().getClientRegistry().deregisterClient(clientId);
        }
    }
}
