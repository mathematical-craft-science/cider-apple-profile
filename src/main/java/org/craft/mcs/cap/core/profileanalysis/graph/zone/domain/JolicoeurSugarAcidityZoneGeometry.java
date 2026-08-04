package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityRegion;
import org.jspecify.annotations.NonNull;


/** Geometry used to classify one representative sugar-acidity graph point. */
public sealed interface JolicoeurSugarAcidityZoneGeometry extends SugarAcidityRegion
    permits RectangleSugarAcidityZoneGeometry,
    EllipseSugarAcidityZoneGeometry {

  @NonNull
  JolicoeurSugarAcidityZone zone();
}
