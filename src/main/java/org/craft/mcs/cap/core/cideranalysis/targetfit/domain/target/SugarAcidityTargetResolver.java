package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneId;
import org.jspecify.annotations.NonNull;


/** Resolves a configured sugar-acidity zone into a concrete scoring target. */
public interface SugarAcidityTargetResolver {

  @NonNull
  PredefinedJolicoeurSugarAcidityZoneTarget resolvePredefinedZone(
      @NonNull JolicoeurSugarAcidityZoneId zone
  );
}
