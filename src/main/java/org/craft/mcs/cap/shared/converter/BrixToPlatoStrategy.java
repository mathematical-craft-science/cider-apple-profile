package org.craft.mcs.cap.shared.converter;

import org.craft.mcs.cap.shared.BrixValue;
import org.craft.mcs.cap.shared.PlatoValue;
import org.craft.mcs.cap.shared.SpecificGravityValue;

public final class BrixToPlatoStrategy
    implements SugarConversionStrategy<BrixValue, PlatoValue> {

  private final BrixToSpecificGravityStrategy brixToSg;
  private final SpecificGravityToPlatoStrategy sgToPlato;

  public BrixToPlatoStrategy(
      BrixToSpecificGravityStrategy brixToSg,
      SpecificGravityToPlatoStrategy sgToPlato
  ) {
    this.brixToSg = brixToSg;
    this.sgToPlato = sgToPlato;
  }

  @Override
  public Class<BrixValue> sourceType() {
    return BrixValue.class;
  }

  @Override
  public Class<PlatoValue> targetType() {
    return PlatoValue.class;
  }

  @Override
  public PlatoValue convert(BrixValue source) {
    SpecificGravityValue sg = brixToSg.convert(source);
    return sgToPlato.convert(sg);
  }
}
