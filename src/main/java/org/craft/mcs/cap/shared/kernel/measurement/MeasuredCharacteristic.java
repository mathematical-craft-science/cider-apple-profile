package org.craft.mcs.cap.shared.kernel.measurement;

import org.jmolecules.ddd.annotation.ValueObject;


@ValueObject
public sealed interface MeasuredCharacteristic<T extends Comparable<T>>
    permits ExactMeasurement, RangeMeasurement, StatisticalMeasurement {
}
