package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityPointDistance;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityRegion;
import org.jspecify.annotations.NonNull;


/** Geometry operations required by sugar-acidity target scoring. */
public interface SugarAcidityTargetGeometry extends SugarAcidityRegion {

  @NonNull
  SugarAcidityGraphPoint nearestPointTo(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityPointDistance distance
  );
}
