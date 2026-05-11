package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import org.jspecify.annotations.NonNull;


public interface SugarAcidityTarget {

  @NonNull
  SugarAcidityTargetKind kind();

  @NonNull
  SugarAcidityTargetGeometry geometry();
}
