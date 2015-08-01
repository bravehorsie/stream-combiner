package net.grigoriadi.sc.transport;

import net.grigoriadi.sc.AppConetxt;
import net.grigoriadi.sc.domain.Item;
import net.grigoriadi.sc.processing.IStreamParser;
import net.grigoriadi.sc.processing.JAXBParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * A client connecting to provided host and port.
 */
public class StreamClientTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StreamClientTask.class);

    //TODO externalize host port and id as ClientId with hashcode
    private final String host;

    private final int port;

    private final Integer id;

    private final IStreamParser streamParser;

    public StreamClientTask(String host, int port, Integer id) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.streamParser = new JAXBParser(item-> {
            AppConetxt.getInstance().getClientRegistry().registerLastClientTime(getClientId(), item.getTime());
            synchronized (this) {
                ConcurrentHashMap<Long, Item> items = AppConetxt.getInstance().getItems();
                Item found = items.computeIfPresent(item.getTime(), new BiFunction<Long, Item, Item>() {
                    @Override
                    public Item apply(Long aLong, Item aItem) {
                        aItem.addAmount(item.getAmount());
                        return aItem;
                    }
                });
                if (found == null) {
                    items.put(item.getTime(), item);
                    AppConetxt.getInstance().getTimeQueue().add(item.getTime());
                }
            }
        });
        AppConetxt.getInstance().getClientRegistry().registerClient(getClientId());
    }

    private String getClientId() {
        return host + ":" + port + "-" + id;
    }

    @Override
    public void run() {
        InputStream clientInputStream = null;
        try {
            Socket client = new Socket(host, port);
            clientInputStream = client.getInputStream();
            streamParser.readStream(clientInputStream);
            clientInputStream.close();
            //TODO feels weird, won't work if clients would be reconnecting
            AppConetxt.getInstance().getClientRegistry().registerLastClientTime(getClientId(), Long.MAX_VALUE);
            LOG.debug("CLOSING CLIENT: " + getClientId());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
