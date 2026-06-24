package org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain;

import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public sealed interface SugarAcidityGraphZone
    permits RectangleSugarAcidityGraphZone, EllipseSugarAcidityGraphZone {

  @NonNull
  SugarAcidityZone zone();

  @NonNull
  SugarAcidityGraphZoneShape shape();
}
