package net.grigoriadi.sc.domain;

import java.math.BigDecimal;

/**
 * Immutable item representation with hashcode.
 */
public class Item implements Comparable<Item> {

    private Long time;

    private BigDecimal amount;

    public Item() {
    }

    public Item(Long time, BigDecimal amount) {
        this.time = time;
        this.amount = amount;
    }

    public Long getTime() {
        return time;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public int compareTo(Item o) {
        return time.compareTo(o.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!time.equals(item.time)) return false;
        return amount.equals(item.amount);

    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }
}
