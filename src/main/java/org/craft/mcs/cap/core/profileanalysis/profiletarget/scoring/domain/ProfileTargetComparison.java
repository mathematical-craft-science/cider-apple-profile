package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Geometry-only comparison between one representative profile point and a target.
 *
 * <p>The comparison owns target containment and nearest-coordinate facts. Distance functions and
 * cider-making contribution assessors interpret the same facts without repeating target geometry.
 */
@ValueObject
public record ProfileTargetComparison(
    @NonNull SugarAcidityGraphPoint point,
    @NonNull SugarAcidityGraphPoint nearestTargetPoint,
    @Nullable TanninValue tannin,
    @Nullable TanninValue nearestTargetTannin,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
) {

  public ProfileTargetComparison {
    requireNonNull(point, "profile target comparison point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest profile target point cannot be null");
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "missing profile target axes cannot be null"));
    if ((tannin == null) != (nearestTargetTannin == null)) {
      throw new DomainRuleViolationException("compared tannin and nearest target tannin must be present together");
    }
    if (missingAxes.stream().anyMatch(axis -> axis != ProfileEvaluationAxis.TANNIN)) {
      throw new DomainRuleViolationException("SG/TA target comparison cannot omit mandatory graph axes");
    }
    if (!missingAxes.isEmpty() && tannin != null) {
      throw new DomainRuleViolationException("missing tannin cannot have compared tannin values");
    }
    if (insideTarget && !insideSugarAcidityTarget) {
      throw new DomainRuleViolationException(
          "profile cannot be inside the full target outside its SG/TA target"
      );
    }
    if (insideTarget && !missingAxes.isEmpty()) {
      throw new DomainRuleViolationException("profile with missing target axes cannot be inside the target");
    }
  }

  public boolean comparable() {
    return missingAxes.isEmpty();
  }
}
