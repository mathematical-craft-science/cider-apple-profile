package org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain;

import org.jspecify.annotations.NonNull;


public interface SugarAcidityTarget {

  @NonNull
  SugarAcidityTargetKind kind();

  @NonNull
  SugarAcidityTargetGeometry geometry();
}
