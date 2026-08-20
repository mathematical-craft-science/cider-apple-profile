package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Target-aware contribution strategy.
 *
 * <p>The strategy compares a source profile with the nearest point of the selected target area. A
 * source inside the target becomes {@link CompositionContributionRole#BALANCED_BASE}. A source
 * above a target coordinate receives the corresponding cider-making contribution role; a source
 * below a TA or tannin target receives a reducing/softening role. The comparison uses canonical SG
 * and TA evidence. It does not infer sensory sweetness or perceived acidity. Tannin is optional
 * and is assessed only when the target includes a tannin range.
 */
public final class TargetAwareCompositionContributionRoleAssessor
    implements CompositionContributionRoleAssessor {

  private static void addSpecificGravityEvidence(
      @NonNull SpecificGravityValue source,
      @NonNull SpecificGravityValue target,
      @NonNull MeasuredValueRangePosition targetPosition,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    final var fact = addEvidence(
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        source,
        target,
        targetPosition,
        evidence
    );
    if (fact.direction() == CompositionContributionEvidenceDirection.ABOVE_TARGET) {
      roles.add(CompositionContributionRole.SUGAR_CONTRIBUTOR);
    } else if (fact.direction() == CompositionContributionEvidenceDirection.BELOW_TARGET) {
      roles.add(CompositionContributionRole.SUGAR_REDUCER);
    }
  }

  private static void addTitratableAcidityEvidence(
      @NonNull TitratableAcidityValue source,
      @NonNull TitratableAcidityValue target,
      @NonNull MeasuredValueRangePosition targetPosition,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    final var fact = addEvidence(
        ProfileEvaluationAxis.TITRATABLE_ACIDITY,
        source,
        target,
        targetPosition,
        evidence
    );
    if (fact.direction() == CompositionContributionEvidenceDirection.ABOVE_TARGET) {
      roles.add(CompositionContributionRole.ACIDITY_CONTRIBUTOR);
    } else if (fact.direction() == CompositionContributionEvidenceDirection.BELOW_TARGET) {
      roles.add(CompositionContributionRole.ACIDITY_REDUCER);
    }
  }

  private static void addTanninEvidence(
      @NonNull ProfileTargetDistance targetDistance,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence,
      @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
  ) {
    missingAxes.addAll(targetDistance.missingAxes());
    final var tannin = targetDistance.tannin();
    final var targetValue = targetDistance.nearestTargetTannin();
    if (tannin == null || targetValue == null) {
      return;
    }

    final var fact = addEvidence(
        ProfileEvaluationAxis.TANNIN,
        tannin,
        targetValue,
        positionOf(tannin, targetValue),
        evidence
    );
    if (fact.direction() == CompositionContributionEvidenceDirection.ABOVE_TARGET) {
      roles.add(CompositionContributionRole.TANNIN_CONTRIBUTOR);
    } else if (fact.direction() == CompositionContributionEvidenceDirection.BELOW_TARGET) {
      roles.add(CompositionContributionRole.TANNIN_SOFTENER);
    }
  }

  private static boolean isDilutionSource(
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    return hasDirection(
        evidence,
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        CompositionContributionEvidenceDirection.BELOW_TARGET
    ) && hasDirection(
        evidence,
        ProfileEvaluationAxis.TITRATABLE_ACIDITY,
        CompositionContributionEvidenceDirection.BELOW_TARGET
    );
  }

  private static boolean hasDirection(
      @NonNull List<@NonNull CompositionContributionEvidence> evidence,
      @NonNull ProfileEvaluationAxis axis,
      @NonNull CompositionContributionEvidenceDirection direction
  ) {
    return evidence.stream()
        .anyMatch(fact -> fact.axis() == axis && fact.direction() == direction);
  }

  private static @NonNull List<@NonNull CompositionContributionRole> orderedRoles(
      @NonNull EnumSet<CompositionContributionRole> roles
  ) {
    return Stream.of(CompositionContributionRole.values())
        .filter(roles::contains)
        .toList();
  }

  private static @NonNull CompositionContributionEvidence addEvidence(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull MeasuredValue source,
      @NonNull MeasuredValue target,
      @NonNull MeasuredValueRangePosition targetPosition,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    final var fact = new CompositionContributionEvidence(axis, source, target, targetPosition);
    evidence.add(fact);
    return fact;
  }

  private static @NonNull MeasuredValueRangePosition positionOf(
      @NonNull TanninValue source,
      @NonNull TanninValue target
  ) {
    return MeasuredValueRangePosition.fromComparison(source.compareTo(target));
  }

  @Override
  public @NonNull CompositionContributionRoleAssessment assess(
      @NonNull CompositionContributionRoleAssessmentRequest request
  ) {
    requireNonNull(request, "composition contribution assessment request cannot be null");

    final var targetDistance = request.targetDistance();
    final var missingAxes = EnumSet.noneOf(ProfileEvaluationAxis.class);
    final var sourcePoint = targetDistance.point();
    final var targetPoint = targetDistance.nearestTargetPoint();
    final var roles = EnumSet.noneOf(CompositionContributionRole.class);
    final var evidence = new ArrayList<CompositionContributionEvidence>();
    final var specificGravity = sourcePoint.specificGravity();
    final var titratableAcidity = sourcePoint.titratableAcidity();
    final var targetSpecificGravity = targetPoint.specificGravity();
    final var targetTitratableAcidity = targetPoint.titratableAcidity();
    final var targetPosition = targetDistance.sugarAcidityTargetPosition();

    addSpecificGravityEvidence(
        specificGravity,
        targetSpecificGravity,
        targetPosition.specificGravity(),
        roles,
        evidence
    );
    addTitratableAcidityEvidence(
        titratableAcidity,
        targetTitratableAcidity,
        targetPosition.titratableAcidity(),
        roles,
        evidence
    );
    addTanninEvidence(targetDistance, roles, evidence, missingAxes);

    if (targetDistance.insideSugarAcidityTarget()) {
      roles.add(CompositionContributionRole.BALANCED_BASE);
    }
    if (isDilutionSource(evidence)) {
      roles.add(CompositionContributionRole.DILUTION_SOURCE);
    }
    return new CompositionContributionRoleAssessment(
        orderedRoles(roles),
        evidence,
        missingAxes
    );
  }
}
