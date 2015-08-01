package net.grigoriadi.sc.processing;

import net.grigoriadi.sc.domain.Item;

/**
 * Marshalls data out.
 */
public interface IDataMarshaller {

    void marshallData(Item data);
}
