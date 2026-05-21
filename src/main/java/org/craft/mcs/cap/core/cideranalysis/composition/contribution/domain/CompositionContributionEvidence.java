package org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Per-axis explanation for a target-aware contribution role.
 *
 * <p>{@code targetCoordinate} is the nearest coordinate on the target geometry for the source
 * point. Positive {@code deviationFromTarget} means the source is above that target coordinate;
 * negative means it is below it.
 */
public record CompositionContributionEvidence(
    @NonNull ProfileEvaluationAxis axis,
    @NonNull BigDecimal sourceValue,
    @NonNull BigDecimal targetCoordinate,
    @NonNull BigDecimal deviationFromTarget,
    @NonNull CompositionContributionRole role
) {

  public CompositionContributionEvidence {
    requireNonNull(axis, "composition contribution evidence axis cannot be null");
    requireNonNull(sourceValue, "composition contribution evidence source value cannot be null");
    requireNonNull(targetCoordinate, "composition contribution evidence target coordinate cannot be null");
    requireNonNull(deviationFromTarget, "composition contribution evidence deviation cannot be null");
    requireNonNull(role, "composition contribution evidence role cannot be null");
  }
}
