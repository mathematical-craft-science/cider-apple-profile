package org.craft.mcs.cap.shared.kernel.sugar.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.kernel.sugar.PlatoValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SpecificGravityToPlatoStrategy
    implements SugarConversionStrategy<SpecificGravityValue, PlatoValue> {

  private static final BigDecimal A = new BigDecimal("135.997");
  private static final BigDecimal B = new BigDecimal("630.272");
  private static final BigDecimal C = new BigDecimal("1111.14");
  private static final BigDecimal D = new BigDecimal("616.868");

  private final MathContext mathContext;

  public SpecificGravityToPlatoStrategy(@NonNull MathContext mathContext) {
    this.mathContext = requireNonNull(mathContext);
  }

  @NonNull
  @Override
  public Class<SpecificGravityValue> sourceType() {
    return SpecificGravityValue.class;
  }

  @NonNull
  @Override
  public Class<PlatoValue> targetType() {
    return PlatoValue.class;
  }

  @NonNull
  @Override
  public PlatoValue convert(@NonNull SpecificGravityValue source) {
    final var sg = source.asBigDecimal();

    final var plato = A.multiply(sg, mathContext)
        .subtract(B, mathContext)
        .multiply(sg, mathContext)
        .add(C, mathContext)
        .multiply(sg, mathContext)
        .subtract(D, mathContext);

    return PlatoValue.of(plato);
  }
}
