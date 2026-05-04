package org.craft.mcs.cap.shared.domain.measurement.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.domain.measurement.value.BrixValue;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SpecificGravityToBrixStrategy
    implements SugarConversionStrategy<SpecificGravityValue, BrixValue> {

  private static final BigDecimal A = new BigDecimal("182.4601");
  private static final BigDecimal B = new BigDecimal("775.6821");
  private static final BigDecimal C = new BigDecimal("1262.7794");
  private static final BigDecimal D = new BigDecimal("669.5622");

  private final MathContext mathContext;

  public SpecificGravityToBrixStrategy(@NonNull MathContext mathContext) {
    this.mathContext = requireNonNull(mathContext);
  }

  @NonNull
  @Override
  public Class<SpecificGravityValue> sourceType() {
    return SpecificGravityValue.class;
  }

  @NonNull
  @Override
  public Class<BrixValue> targetType() {
    return BrixValue.class;
  }

  @NonNull
  @Override
  public BrixValue convert(@NonNull SpecificGravityValue source) {
    final var sg = source.asBigDecimal();

    final var result = A.multiply(sg.pow(3, mathContext), mathContext)
        .subtract(B.multiply(sg.pow(2, mathContext), mathContext), mathContext)
        .add(C.multiply(sg, mathContext), mathContext)
        .subtract(D, mathContext);

    return BrixValue.of(result);
  }
}

