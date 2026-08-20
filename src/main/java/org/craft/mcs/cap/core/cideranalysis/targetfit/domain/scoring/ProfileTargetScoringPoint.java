package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Representative SG/TA graph point with optional tannin used by target scoring. */
@ValueObject
public record ProfileTargetScoringPoint(
    @NonNull SugarAcidityGraphPoint sugarAcidityPoint,
    @Nullable TanninValue tannin
) {

  public ProfileTargetScoringPoint {
    requireNonNull(sugarAcidityPoint, "target-scoring sugar-acidity point cannot be null");
  }
}
