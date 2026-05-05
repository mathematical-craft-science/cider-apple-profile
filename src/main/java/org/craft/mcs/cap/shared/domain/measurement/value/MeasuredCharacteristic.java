package org.craft.mcs.cap.shared.domain.measurement.value;

import java.util.Optional;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public sealed interface MeasuredCharacteristic<T extends Comparable<T>>
    permits ExactMeasurement, RangeMeasurement, StatisticalMeasurement {

  @NonNull
  Optional<@NonNull T> representativeValue();
}
