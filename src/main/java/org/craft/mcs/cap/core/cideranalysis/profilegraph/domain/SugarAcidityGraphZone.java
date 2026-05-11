package org.craft.mcs.cap.core.cideranalysis.profilegraph.domain;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public sealed interface SugarAcidityGraphZone
    permits RectangleSugarAcidityGraphZone, EllipseSugarAcidityGraphZone {

  @NonNull
  SugarAcidityZone zone();

  @NonNull
  SugarAcidityGraphZoneShape shape();
}
