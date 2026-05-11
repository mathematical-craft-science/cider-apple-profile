package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public sealed interface SugarAcidityZoneGeometry
    permits RectangleSugarAcidityZoneGeometry, EllipseSugarAcidityZoneGeometry {

  @NonNull
  SugarAcidityZone zone();

  @NonNull
  SugarAcidityZoneGeometryShape shape();

  boolean contains(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  );
}
