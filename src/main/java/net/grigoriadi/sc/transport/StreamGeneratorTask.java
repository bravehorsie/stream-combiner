package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.processing.IStreamGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Generates dummy stream for clients.
 */
public class StreamGeneratorTask implements Runnable {

    private final Socket clientSocket;

    private final IStreamGenerator streamWriter;

    public StreamGeneratorTask(Socket clientSocket, IStreamGenerator streamWriter) {
        this.clientSocket = clientSocket;
        this.streamWriter = streamWriter;
    }

    @Override
    public void run() {
        try {
            OutputStream out = clientSocket.getOutputStream();
            streamWriter.writeStream(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
