package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppConetxt;
import net.grigoriadi.sc.domain.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A handler for a parsed item.
 */
public class ItemHandler implements Consumer<Item> {

    private final String clientId;

    private static final Object lock = new Object();

    public ItemHandler(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Accepts an item from {@link IStreamParser} and propagates it in queue, registers metadatk
     * @param item
     */
    @Override
    public void accept(Item item) {
        AppConetxt.getInstance().getClientRegistry().registerLastClientTime(clientId, item.getTime());
        ConcurrentHashMap<Long, Item> items = AppConetxt.getInstance().getItems();
        items.compute(item.getTime(), new BiFunction<Long, Item, Item>() {
            @Override
            public Item apply(Long aLong, Item aItem) {
                if (aItem == null) {
                    AppConetxt.getInstance().getTimeQueue().add(item.getTime());
                    return item;
                } else {
                    aItem.addAmount(item.getAmount());
                    return aItem;
                }
            }
        });
    }
}
