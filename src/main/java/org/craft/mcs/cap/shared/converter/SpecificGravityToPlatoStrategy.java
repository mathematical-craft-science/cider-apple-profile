package org.craft.mcs.cap.shared.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.PlatoValue;
import org.craft.mcs.cap.shared.SpecificGravityValue;

public final class SpecificGravityToPlatoStrategy
    implements SugarConversionStrategy<SpecificGravityValue, PlatoValue> {

  private static final BigDecimal A = new BigDecimal("135.997");
  private static final BigDecimal B = new BigDecimal("630.272");
  private static final BigDecimal C = new BigDecimal("1111.14");
  private static final BigDecimal D = new BigDecimal("616.868");

  private final MathContext mathContext;

  public SpecificGravityToPlatoStrategy(MathContext mathContext) {
    this.mathContext = mathContext;
  }

  @Override
  public Class<SpecificGravityValue> sourceType() {
    return SpecificGravityValue.class;
  }

  @Override
  public Class<PlatoValue> targetType() {
    return PlatoValue.class;
  }

  @Override
  public PlatoValue convert(SpecificGravityValue source) {
    BigDecimal sg = source.asBigDecimal();

    BigDecimal plato = A.multiply(sg, mathContext)
        .subtract(B, mathContext)
        .multiply(sg, mathContext)
        .add(C, mathContext)
        .multiply(sg, mathContext)
        .subtract(D, mathContext);

    return PlatoValue.of(plato);
  }
}
