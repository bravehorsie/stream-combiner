package net.grigoriadi.sc.processing.generation;

import java.math.BigDecimal;

/**
 * Listener for a generated item during server stream generation.
 */
public interface IItemWrittenListener {

    /**
     * Handle item data..
     */
    void itemWritten(Long time, BigDecimal amount);
}
