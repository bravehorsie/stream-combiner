package net.grigoriadi.sc.processing.generation;

import net.grigoriadi.sc.processing.XmlDataBindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Generates dummy stream for clients.
 */
public class StreamGeneratorTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StreamGeneratorTask.class);

    private final Socket clientSocket;

    private final IStreamGenerator streamWriter;

    public StreamGeneratorTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.streamWriter = XmlDataBindingFactory.newStreamGenerator();
    }

    @Override
    public void run() {
        try {
            OutputStream out = clientSocket.getOutputStream();
            streamWriter.writeStream(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            LOG.error("Error writing data", e);
            throw new RuntimeException(e);
        }
        LOG.info("Exiting stream generator");
    }
}
