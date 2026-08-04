package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.jspecify.annotations.NonNull;


/** SG/TA target selected for candidate scoring. */
public interface SugarAcidityTarget {

  @NonNull
  SugarAcidityTargetKind kind();

  @NonNull
  SugarAcidityTargetGeometry geometry();
}
