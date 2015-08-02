package net.grigoriadi.sc.processing.generation;

import java.math.BigDecimal;

/**
 * Listener for a generated amount during server stream generation.
 */
public interface IItemWrittenListener {

    void itemWritten(Long time, BigDecimal amount);
}
