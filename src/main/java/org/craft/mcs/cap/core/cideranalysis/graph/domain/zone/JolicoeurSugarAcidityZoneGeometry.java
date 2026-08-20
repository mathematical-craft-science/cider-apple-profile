package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityRegion;
import org.jspecify.annotations.NonNull;


/** Geometry used to classify one representative sugar-acidity graph point. */
public sealed interface JolicoeurSugarAcidityZoneGeometry extends SugarAcidityRegion
    permits RectangleSugarAcidityZoneGeometry,
    EllipseSugarAcidityZoneGeometry {

  @NonNull
  JolicoeurSugarAcidityZoneId zone();
}
