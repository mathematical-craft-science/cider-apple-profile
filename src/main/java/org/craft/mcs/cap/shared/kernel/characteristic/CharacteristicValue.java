package org.craft.mcs.cap.shared.kernel.characteristic;

import org.jmolecules.ddd.annotation.ValueObject;


/**
 * Describes the shape of one typed characteristic value without measurement evidence semantics.
 *
 * <p>Provenance, time, confidence and derivation belong to the context that uses the value.</p>
 *
 * @param <T> comparable value-object type of the characteristic
 */
@ValueObject
public sealed interface CharacteristicValue<T extends Comparable<? super T>>
    permits PointCharacteristicValue, IntervalCharacteristicValue {
}
