package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import org.jspecify.annotations.NonNull;


public interface SugarAcidityTargetGeometry {

  @NonNull
  SugarAcidityTargetGeometryShape shape();

  boolean contains(@NonNull SugarAcidityCoordinates point);

  @NonNull
  SugarAcidityCoordinates nearestPointTo(@NonNull SugarAcidityCoordinates point);
}
