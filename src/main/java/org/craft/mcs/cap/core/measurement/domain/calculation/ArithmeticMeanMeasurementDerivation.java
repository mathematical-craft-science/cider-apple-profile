package org.craft.mcs.cap.core.measurement.domain.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Derives the canonical arithmetic-mean result from repeated point observations. */
public final class ArithmeticMeanMeasurementDerivation {

  private static final int CALCULATION_SCALE = 12;

  private ArithmeticMeanMeasurementDerivation() {
  }

  public static @NonNull PointCharacteristicValue<MeasuredValue> derive(
      @NonNull List<@NonNull MeasuredValue> observations
  ) {
    final var values = List.copyOf(requireNonNull(
        observations,
        "measurement observations cannot be null"
    ));
    if (values.size() < 2) {
      throw new DomainRuleViolationException("arithmetic mean requires at least two observations");
    }

    final var first = values.getFirst();
    if (values.stream().anyMatch(value -> value.getClass() != first.getClass())) {
      throw new DomainRuleViolationException(
          "arithmetic mean observations must measure the same characteristic"
      );
    }

    final var average = values.stream()
        .map(MeasuredValue::asBigDecimal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(values.size()), CALCULATION_SCALE, RoundingMode.HALF_UP)
        .stripTrailingZeros();
    return PointCharacteristicValue.of(first.withValue(average));
  }
}
