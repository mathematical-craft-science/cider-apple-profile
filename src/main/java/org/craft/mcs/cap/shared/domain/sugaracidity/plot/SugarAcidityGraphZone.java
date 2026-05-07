package org.craft.mcs.cap.shared.domain.sugaracidity.plot;

import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public sealed interface SugarAcidityGraphZone
    permits RectangleSugarAcidityGraphZone, EllipseSugarAcidityGraphZone {

  @NonNull
  SugarAcidityZone zone();

  @NonNull
  SugarAcidityGraphZoneShape shape();
}
