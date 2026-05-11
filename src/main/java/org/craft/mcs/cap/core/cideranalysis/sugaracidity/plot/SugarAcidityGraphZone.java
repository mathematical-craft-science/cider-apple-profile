package org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot;

import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public sealed interface SugarAcidityGraphZone
    permits RectangleSugarAcidityGraphZone, EllipseSugarAcidityGraphZone {

  @NonNull
  SugarAcidityZone zone();

  @NonNull
  SugarAcidityGraphZoneShape shape();
}
