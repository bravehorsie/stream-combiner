package net.grigoriadi.sc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Supporting server generating random streams for clients.
 */
public class StreamServerTask extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StreamClientTask.class);

    private final ExecutorService executorService;

    private static final int MAX_CONN = 500;
    private ServerSocket serverSocket;

    public StreamServerTask() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9123);
            int connections = 0;
            while (connections++ < MAX_CONN) {
                Socket accept = serverSocket.accept();
                LOG.debug("Accepted connection from client");
                executorService.execute(new StreamGeneratorTask(accept));
            }

            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Error writing to socket");
        }
        LOG.debug("Exiting server");
    }

    @Override
    public void interrupt() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Server socket closing failed", e);
            throw new RuntimeException(e);
        } finally {
            super.interrupt();
        }
    }


}
