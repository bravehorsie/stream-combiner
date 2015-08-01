package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.processing.StaxStreamGenerator;
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
public class StreamServerTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(StreamClientTask.class);

    private final ExecutorService executorService;

    private static final int MAX_CONN = 500;

    public StreamServerTask() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            int connections = 0;
            while (connections++ < MAX_CONN) {
                Socket accept = serverSocket.accept();
                LOG.debug("Accepted connection from client");
                executorService.execute(new StreamGeneratorTask(accept, new StaxStreamGenerator()));
            }

            LOG.debug("CLOSING SERVER");
//            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        //TODO delete this or handle interruption correctly.
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
