package net.grigoriadi.sc.processing.parsing;

import net.grigoriadi.sc.AppContext;
import net.grigoriadi.sc.domain.Item;

import java.math.BigDecimal;
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
     * If there is a record for a given time in Items map, just sum amounts.
     * If item with given time is new, put it to map and propagate time of it to a queue for further processing.
     * @param item
     */
    @Override
    public void accept(Item item) {
        AppContext.getInstance().getClientRegistry().registerLastClientTime(clientId, item.getTime());
        ConcurrentHashMap<Long, Item> items = AppContext.getInstance().getSummedItems();
        items.compute(item.getTime(), (aLong, aItem) -> {
            if (aItem == null) {
                AppContext.getInstance().getWorkQueue().put(item.getTime());
                return item;
            } else {
                BigDecimal addedAmount = aItem.getAmount().add(item.getAmount());
                return new Item(aItem.getTime(), addedAmount);
            }
        });
    }
}
