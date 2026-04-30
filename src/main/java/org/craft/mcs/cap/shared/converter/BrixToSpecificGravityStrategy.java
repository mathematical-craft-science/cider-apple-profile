package org.craft.mcs.cap.shared.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.BrixValue;
import org.craft.mcs.cap.shared.SpecificGravityValue;


public final class BrixToSpecificGravityStrategy
    implements SugarConversionStrategy<BrixValue, SpecificGravityValue> {

  private static final BigDecimal REF_1 = new BigDecimal("258.6");
  private static final BigDecimal REF_2 = new BigDecimal("258.2");
  private static final BigDecimal REF_3 = new BigDecimal("227.1");

  private final MathContext mathContext;

  public BrixToSpecificGravityStrategy(MathContext mathContext) {
    this.mathContext = mathContext;
  }

  @Override
  public Class<BrixValue> sourceType() {
    return BrixValue.class;
  }

  @Override
  public Class<SpecificGravityValue> targetType() {
    return SpecificGravityValue.class;
  }

  @Override
  public SpecificGravityValue convert(BrixValue source) {
    BigDecimal brix = source.asBigDecimal();

    BigDecimal denominator = REF_1.subtract(
        brix.multiply(REF_3, mathContext)
            .divide(REF_2, mathContext),
        mathContext
    );

    BigDecimal sg = brix.divide(denominator, mathContext)
        .add(BigDecimal.ONE, mathContext);

    return SpecificGravityValue.of(sg);
  }
}
