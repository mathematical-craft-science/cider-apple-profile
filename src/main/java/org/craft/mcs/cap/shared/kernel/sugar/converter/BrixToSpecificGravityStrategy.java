package org.craft.mcs.cap.shared.kernel.sugar.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.kernel.sugar.BrixValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class BrixToSpecificGravityStrategy
    implements SugarConversionStrategy<BrixValue, SpecificGravityValue> {

  private static final BigDecimal REF_1 = new BigDecimal("258.6");
  private static final BigDecimal REF_2 = new BigDecimal("258.2");
  private static final BigDecimal REF_3 = new BigDecimal("227.1");

  private final MathContext mathContext;

  public BrixToSpecificGravityStrategy(@NonNull MathContext mathContext) {
    this.mathContext = requireNonNull(mathContext);
  }

  @Override
  @NonNull
  public Class<BrixValue> sourceType() {
    return BrixValue.class;
  }

  @Override
  @NonNull
  public Class<SpecificGravityValue> targetType() {
    return SpecificGravityValue.class;
  }

  @Override
  @NonNull
  public SpecificGravityValue convert(@NonNull BrixValue source) {
    requireNonNull(source);
    final var brix = source.asBigDecimal();

    final var denominator = REF_1.subtract(
        brix.multiply(REF_3, mathContext)
            .divide(REF_2, mathContext),
        mathContext
    );

    final var sg = brix.divide(denominator, mathContext)
        .add(BigDecimal.ONE, mathContext);

    return SpecificGravityValue.of(sg);
  }
}
