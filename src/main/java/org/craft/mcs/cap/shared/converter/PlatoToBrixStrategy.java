package org.craft.mcs.cap.shared.converter;

import org.craft.mcs.cap.shared.BrixValue;
import org.craft.mcs.cap.shared.PlatoValue;
import org.craft.mcs.cap.shared.SpecificGravityValue;

public final class PlatoToBrixStrategy
    implements SugarConversionStrategy<PlatoValue, BrixValue> {

  private final PlatoToSpecificGravityStrategy platoToSg;
  private final SpecificGravityToBrixStrategy sgToBrix;

  public PlatoToBrixStrategy(
      PlatoToSpecificGravityStrategy platoToSg,
      SpecificGravityToBrixStrategy sgToBrix
  ) {
    this.platoToSg = platoToSg;
    this.sgToBrix = sgToBrix;
  }

  @Override
  public Class<PlatoValue> sourceType() {
    return PlatoValue.class;
  }

  @Override
  public Class<BrixValue> targetType() {
    return BrixValue.class;
  }

  @Override
  public BrixValue convert(PlatoValue source) {
    SpecificGravityValue sg = platoToSg.convert(source);
    return sgToBrix.convert(sg);
  }
}
