package org.craft.mcs.cap.shared.domain.measurement.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.domain.measurement.value.PlatoValue;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Approximate Plato → Specific Gravity conversion.
 * Not exact inverse of ASBC SG → Plato polynomial.
 */
public final class PlatoToSpecificGravityStrategy
    implements SugarConversionStrategy<PlatoValue, SpecificGravityValue> {

  private static final BigDecimal REF_1 = new BigDecimal("258.6");
  private static final BigDecimal REF_2 = new BigDecimal("258.2");
  private static final BigDecimal REF_3 = new BigDecimal("227.1");

  private final MathContext mathContext;

  public PlatoToSpecificGravityStrategy(@NonNull MathContext mathContext) {
    this.mathContext = requireNonNull(mathContext);
  }

  @NonNull
  @Override
  public Class<PlatoValue> sourceType() {
    return PlatoValue.class;
  }

  @NonNull
  @Override
  public Class<SpecificGravityValue> targetType() {
    return SpecificGravityValue.class;
  }

  @NonNull
  @Override
  public SpecificGravityValue convert(@NonNull PlatoValue source) {
    requireNonNull(source);
    final var plato = source.asBigDecimal();

    final var denominator = REF_1.subtract(
        plato.multiply(REF_3, mathContext)
            .divide(REF_2, mathContext),
        mathContext
    );

    final var sg = plato.divide(denominator, mathContext)
        .add(BigDecimal.ONE, mathContext);

    return SpecificGravityValue.of(sg);
  }
}
