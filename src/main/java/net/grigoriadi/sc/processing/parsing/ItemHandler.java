package net.grigoriadi.sc.processing.parsing;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.sc.domain.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A handler for a parsed item.
 */
public class ItemHandler implements Consumer<Item> {

    private final String clientId;

    public ItemHandler(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Accepts an item from {@link IStreamParser} and propagates it in queue and registers metadata for queue worker.
     * @param item
     */
    @Override
    public void accept(Item item) {
        AppContext.getInstance().getClientRegistry().registerLastClientTime(clientId, item.getTime());
        ConcurrentHashMap<Long, Item> items = AppContext.getInstance().getItemSums();
        items.compute(item.getTime(), (aLong, aItem) -> {
            if (aItem == null) {
                AppContext.getInstance().getWorkQueue().add(item.getTime());
                return item;
            } else {
                aItem.addAmount(item.getAmount());
                return aItem;
            }
        });
    }
}
