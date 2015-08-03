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
        OutputStream out = null;
        try {
            out = clientSocket.getOutputStream();
            streamWriter.writeStream(out);
            out.flush();
        } catch (IOException e) {
            LOG.error("Error writing data", e);
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.info("Exiting stream generator");
    }
}
