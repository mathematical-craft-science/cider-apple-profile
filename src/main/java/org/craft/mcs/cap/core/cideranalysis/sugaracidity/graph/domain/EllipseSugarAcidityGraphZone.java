package org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record EllipseSugarAcidityGraphZone(
    @NonNull SugarAcidityZone zone,
    @NonNull BigDecimal sugarCenter,
    @NonNull BigDecimal acidityCenter,
    @NonNull BigDecimal sugarRadius,
    @NonNull BigDecimal acidityRadius
) implements SugarAcidityGraphZone {

  public EllipseSugarAcidityGraphZone {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(sugarCenter, "ellipse sugar center cannot be null");
    requireNonNull(acidityCenter, "ellipse acidity center cannot be null");
    requireNonNull(sugarRadius, "ellipse sugar radius cannot be null");
    requireNonNull(acidityRadius, "ellipse acidity radius cannot be null");
  }

  @Override
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.ELLIPSE;
  }
}
