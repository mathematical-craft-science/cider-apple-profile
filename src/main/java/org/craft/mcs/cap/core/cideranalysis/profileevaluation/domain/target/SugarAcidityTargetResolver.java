package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;


public interface SugarAcidityTargetResolver {

  @NonNull
  PredefinedSugarAcidityZoneTarget resolvePredefinedZone(@NonNull SugarAcidityZone zone);
}
