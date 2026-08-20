package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import org.jspecify.annotations.NonNull;


/** Minimal geometric region in the shared SG/TA coordinate space. */
public interface SugarAcidityRegion {

  @NonNull
  SugarAcidityGeometryShape shape();

  boolean contains(@NonNull SugarAcidityGraphPoint point);
}
