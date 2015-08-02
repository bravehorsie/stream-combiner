package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.AppContext;

import java.math.BigDecimal;

/**
 * Sums generated amounts to application context.
 */
public class SumAmountsListener implements IItemWrittenListener {
    @Override
    public void itemWritten(Long time, BigDecimal amount) {
        AppContext.getInstance().addToTotalGeneratedAmount(amount);
    }
}
