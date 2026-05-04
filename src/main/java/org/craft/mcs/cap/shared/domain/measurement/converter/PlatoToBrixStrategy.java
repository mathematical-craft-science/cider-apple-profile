package org.craft.mcs.cap.shared.domain.measurement.converter;

import org.craft.mcs.cap.shared.domain.measurement.value.BrixValue;
import org.craft.mcs.cap.shared.domain.measurement.value.PlatoValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class PlatoToBrixStrategy
    implements SugarConversionStrategy<PlatoValue, BrixValue> {

  private final PlatoToSpecificGravityStrategy platoToSg;
  private final SpecificGravityToBrixStrategy sgToBrix;

  public PlatoToBrixStrategy(
      @NonNull PlatoToSpecificGravityStrategy platoToSg,
      @NonNull SpecificGravityToBrixStrategy sgToBrix
  ) {
    this.platoToSg = requireNonNull(platoToSg);
    this.sgToBrix = requireNonNull(sgToBrix);
  }

  @NonNull
  @Override
  public Class<PlatoValue> sourceType() {
    return PlatoValue.class;
  }

  @NonNull
  @Override
  public Class<BrixValue> targetType() {
    return BrixValue.class;
  }

  @NonNull
  @Override
  public BrixValue convert(@NonNull PlatoValue source) {
    requireNonNull(source);
    final var sg = platoToSg.convert(source);
    return sgToBrix.convert(sg);
  }
}
