package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.NonNull;


/** Resolves a configured sugar-acidity zone into a concrete scoring target. */
public interface SugarAcidityTargetResolver {

  @NonNull
  PredefinedJolicoeurSugarAcidityZoneTarget resolvePredefinedZone(
      @NonNull JolicoeurSugarAcidityZone zone
  );
}
