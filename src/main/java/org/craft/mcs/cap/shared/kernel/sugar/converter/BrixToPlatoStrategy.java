package org.craft.mcs.cap.shared.kernel.sugar.converter;

import org.craft.mcs.cap.shared.kernel.sugar.BrixValue;
import org.craft.mcs.cap.shared.kernel.sugar.PlatoValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class BrixToPlatoStrategy
    implements SugarConversionStrategy<BrixValue, PlatoValue> {

  private final BrixToSpecificGravityStrategy brixToSg;
  private final SpecificGravityToPlatoStrategy sgToPlato;

  public BrixToPlatoStrategy(
      @NonNull BrixToSpecificGravityStrategy brixToSg,
      @NonNull SpecificGravityToPlatoStrategy sgToPlato
  ) {
    this.brixToSg = requireNonNull(brixToSg);
    this.sgToPlato = requireNonNull(sgToPlato);
  }

  @Override
  @NonNull
  public Class<BrixValue> sourceType() {
    return BrixValue.class;
  }

  @Override
  @NonNull
  public Class<PlatoValue> targetType() {
    return PlatoValue.class;
  }

  @Override
  @NonNull
  public PlatoValue convert(@NonNull BrixValue source) {
    requireNonNull(source);
    final var sg = brixToSg.convert(source);
    return sgToPlato.convert(sg);
  }
}
