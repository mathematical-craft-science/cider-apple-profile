package org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain;

import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public interface SugarAcidityTargetResolver {

  @NonNull
  PredefinedSugarAcidityZoneTarget resolvePredefinedZone(@NonNull SugarAcidityZone zone);
}
