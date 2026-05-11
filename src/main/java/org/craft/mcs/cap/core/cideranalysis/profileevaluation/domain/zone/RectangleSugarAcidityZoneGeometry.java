package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record RectangleSugarAcidityZoneGeometry(
    @NonNull SugarAcidityZone zone,
    @NonNull SugarAcidityZoneRange sugarRange,
    @NonNull SugarAcidityZoneRange acidityRange
) implements SugarAcidityZoneGeometry {

  public RectangleSugarAcidityZoneGeometry {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(sugarRange, "zone sugar range cannot be null");
    requireNonNull(acidityRange, "zone acidity range cannot be null");
  }

  @Override
  public @NonNull SugarAcidityZoneGeometryShape shape() {
    return SugarAcidityZoneGeometryShape.RECTANGLE;
  }

  @Override
  public boolean contains(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  ) {
    requireNonNull(sugar, "sugar value cannot be null");
    requireNonNull(acidity, "acidity value cannot be null");
    return sugarRange.contains(sugar) && acidityRange.contains(acidity);
  }
}
