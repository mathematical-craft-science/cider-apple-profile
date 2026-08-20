package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.jspecify.annotations.NonNull;


/** SG/TA target selected for candidate scoring. */
public interface SugarAcidityTarget {

  @NonNull
  SugarAcidityTargetKind kind();

  @NonNull
  SugarAcidityTargetGeometry geometry();
}
