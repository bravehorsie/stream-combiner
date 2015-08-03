package net.grigoriadi.sc;

import net.grigoriadi.sc.processing.QueueWorker;
import net.grigoriadi.sc.transport.StreamClientTask;
import net.grigoriadi.sc.transport.StreamServerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.concurrent.*;

/**
 * Application developed as homework for Oracle on combining sorted streams.
 */
public class StreamCombinerApp {

    private static Logger LOG = LoggerFactory.getLogger(StreamCombinerApp.class);

    private ExecutorService clientsExecutorService = Executors.newFixedThreadPool(20);

    private Thread serverThread = new StreamServerTask();

    private Thread queueWorkerThread;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("|------------------------------------------------------------------------------|");
            System.out.println("| Usage:                                                                       |");
            System.out.println("| Run a program with arguments in format \"host:port\" separated by spaces.    |");
            System.out.println("| As many clients will be spawned, as many words separated by spaces           |");
            System.out.println("| are provided for a single localhost:constant_port.                           |");
            System.out.println("| (Since app is actually runs one generating server                            |");
            System.out.println("| on a localhost single port, you can type any words separated by spaces).     |");
            System.out.println("|------------------------------------------------------------------------------|");
            System.exit(0);
        }
        StreamCombinerApp app = new StreamCombinerApp();
        System.out.println("|------------------------------------------------------------------------------|");
        System.out.println("| Pressing enter will start a server, clients and a single queue worker.       |");
        System.out.println("| Pressing enter again will terminate all spawned threads                      |");
        System.out.println("| and stop a queue worker after queue of remaining items is drained.           |");
        System.out.println("| Sum of amounts received, should always equal sum of amounts generated,       |");
        System.out.println("| besides the fact if application is terminated before completion.             |");
        System.out.println("| Generating data amount per client connection could be specified              |");
        System.out.println("| in app.properties files. (Defaulted to 100.000 x 10 clients)                 |");
        System.out.println("|------------------------------------------------------------------------------|");
        app.waitForKeyPress(() -> LOG.debug("Starting server and clients..."));
        app.runServer();
        //in the description of the app it is actually mentioned to run clients based on N hosts:ports.
        //since dummy server generating data listens on one single localhost port it doesn't make sense to do so.
        app.runClients(args.length);
        app.runQueueWorker();
        //terminate app on second enter press.
        app.waitForKeyPress(app::terminate);

        LOG.debug("Exiting main thread");
    }

    private void terminate() {
        serverThread.interrupt();
        clientsExecutorService.shutdownNow();
        try {
            clientsExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queue worker is run in separate thread, so it is not interrupted when executor service is requested to shutdown.
     */
    private void runQueueWorker() {
        QueueWorker task = new QueueWorker();
        FutureTask<?> future = new FutureTask<Void>(task, null);
        queueWorkerThread = new Thread(future);
        queueWorkerThread.start();
    }

    /**
     * Run as many clients as requested.
     */
    private void runClients(int count) {
        for (int i = 0; i < count; i++) {
            LOG.debug(MessageFormat.format("Running client on [{0}:{1}], id:[{2}]", "localhost", 9123, i));
            clientsExecutorService.execute(new StreamClientTask("localhost", 9123, i));
        }
    }

    /**
     * Server thread has its own interrupt implementation.
     */
    private void runServer() {
        serverThread.start();
    }


    private void waitForKeyPress(Runnable afterKeyPress) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Press Enter to continue:");
        try {
            br.readLine();
            afterKeyPress.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
