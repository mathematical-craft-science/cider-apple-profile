package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


/**
 * Distance policy used when a graph geometry must select a nearest SG/TA point.
 */
@FunctionalInterface
public interface SugarAcidityPointDistance {

  @NonNull
  BigDecimal between(
      @NonNull SugarAcidityGraphPoint source,
      @NonNull SugarAcidityGraphPoint target
  );
}
