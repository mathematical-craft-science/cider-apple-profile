package org.craft.mcs.cap.shared.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.PlatoValue;
import org.craft.mcs.cap.shared.SpecificGravityValue;


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

  public PlatoToSpecificGravityStrategy(MathContext mathContext) {
    this.mathContext = mathContext;
  }

  @Override
  public Class<PlatoValue> sourceType() {
    return PlatoValue.class;
  }

  @Override
  public Class<SpecificGravityValue> targetType() {
    return SpecificGravityValue.class;
  }

  @Override
  public SpecificGravityValue convert(PlatoValue source) {
    BigDecimal plato = source.asBigDecimal();

    BigDecimal denominator = REF_1.subtract(
        plato.multiply(REF_3, mathContext)
            .divide(REF_2, mathContext),
        mathContext
    );

    BigDecimal sg = plato.divide(denominator, mathContext)
        .add(BigDecimal.ONE, mathContext);

    return SpecificGravityValue.of(sg);
  }
}
