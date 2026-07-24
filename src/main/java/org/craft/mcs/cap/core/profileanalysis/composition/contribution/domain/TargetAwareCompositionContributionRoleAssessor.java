package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Target-aware contribution strategy.
 *
 * <p>The strategy compares a source profile with the nearest point of the selected target area. A
 * source inside the target becomes {@link CompositionContributionRole#BALANCED_BASE}. A source above a target
 * coordinate receives a contributor role for that axis; a source below acidity or tannin target receives a
 * softening/reducing role. Sugar and acidity are required. Tannin is optional and is assessed only when the target
 * includes a tannin range.
 */
public final class TargetAwareCompositionContributionRoleAssessor
    implements CompositionContributionRoleAssessor {

  private static @NonNull Set<@NonNull ChemicalAxis> missingRequiredAxes(
      @NonNull ChemicalProfileAxes axes
  ) {
    final var missing = EnumSet.noneOf(ChemicalAxis.class);
    if (!axes.has(ChemicalAxis.SUGAR)) {
      missing.add(ChemicalAxis.SUGAR);
    }
    if (!axes.has(ChemicalAxis.ACIDITY)) {
      missing.add(ChemicalAxis.ACIDITY);
    }
    return missing;
  }

  private static @NonNull CompositionContributionRoleAssessment unassessable(
      @NonNull Set<@NonNull ChemicalAxis> missingAxes
  ) {
    return new CompositionContributionRoleAssessment(
        List.of(CompositionContributionRole.UNASSESSABLE),
        CompositionContributionRole.UNASSESSABLE,
        List.of(),
        missingAxes
    );
  }

  private static void addSugarEvidence(
      @NonNull BigDecimal source,
      @NonNull BigDecimal target,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    final var deviation = source.subtract(target);
    if (TargetDeviation.of(deviation).isAboveTarget()) {
      roles.add(CompositionContributionRole.SUGAR_CONTRIBUTOR);
      evidence.add(
          evidence(
              ProfileEvaluationAxis.SUGAR,
              source,
              target,
              deviation,
              CompositionContributionRole.SUGAR_CONTRIBUTOR
          )
      );
    }
  }

  private static void addAcidityEvidence(
      @NonNull BigDecimal source,
      @NonNull BigDecimal target,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence
  ) {
    final var deviation = source.subtract(target);
    final var targetDeviation = TargetDeviation.of(deviation);
    if (targetDeviation.isAboveTarget()) {
      roles.add(CompositionContributionRole.ACIDITY_CONTRIBUTOR);
      evidence.add(
          evidence(
              ProfileEvaluationAxis.ACIDITY,
              source,
              target,
              deviation,
              CompositionContributionRole.ACIDITY_CONTRIBUTOR
          )
      );
    } else if (targetDeviation.isBelowTarget()) {
      roles.add(CompositionContributionRole.ACIDITY_REDUCER);
      evidence.add(
          evidence(
              ProfileEvaluationAxis.ACIDITY,
              source,
              target,
              deviation,
              CompositionContributionRole.ACIDITY_REDUCER
          )
      );
    }
  }

  private static void addTanninEvidence(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ProfileTargetObjective target,
      @NonNull EnumSet<CompositionContributionRole> roles,
      @NonNull List<@NonNull CompositionContributionEvidence> evidence,
      @NonNull Set<@NonNull ChemicalAxis> missingAxes
  ) {
    final var tanninTarget = target.tanninTarget();
    if (tanninTarget == null) {
      return;
    }

    final var tannin = nullableValueOf(axes, ChemicalAxis.TANNIN);
    if (tannin == null) {
      missingAxes.add(ChemicalAxis.TANNIN);
      return;
    }

    final var targetValue = tanninTarget.nearestValueTo(tannin);
    final var deviation = tannin.subtract(targetValue);
    final var targetDeviation = TargetDeviation.of(deviation);
    if (targetDeviation.isAboveTarget()) {
      roles.add(CompositionContributionRole.TANNIN_CONTRIBUTOR);
      evidence.add(
          evidence(
              ProfileEvaluationAxis.TANNIN,
              tannin,
              targetValue,
              deviation,
              CompositionContributionRole.TANNIN_CONTRIBUTOR
          )
      );
    } else if (targetDeviation.isBelowTarget()) {
      roles.add(CompositionContributionRole.TANNIN_SOFTENER);
      evidence.add(
          evidence(
              ProfileEvaluationAxis.TANNIN,
              tannin,
              targetValue,
              deviation,
              CompositionContributionRole.TANNIN_SOFTENER
          )
      );
    }
  }

  private static boolean targetContains(
      @NonNull SugarAcidityCoordinates sourcePoint,
      @NonNull ChemicalProfileAxes axes,
      @NonNull ProfileTargetObjective target
  ) {
    final var sugarAcidityMatches = target.sugarAcidityTarget().geometry().contains(sourcePoint);
    final var tanninTarget = target.tanninTarget();
    if (!sugarAcidityMatches || tanninTarget == null) {
      return sugarAcidityMatches;
    }

    final var tannin = nullableValueOf(axes, ChemicalAxis.TANNIN);
    return tannin != null && tanninTarget.contains(tannin);
  }

  private static boolean isDilutionSource(
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal targetSugar,
      @NonNull BigDecimal acidity,
      @NonNull BigDecimal targetAcidity
  ) {
    return TargetDeviation.of(sugar.subtract(targetSugar)).isBelowTarget()
        && TargetDeviation.of(acidity.subtract(targetAcidity)).isBelowTarget();
  }

  private static @NonNull List<@NonNull CompositionContributionRole> orderedRoles(
      @NonNull EnumSet<CompositionContributionRole> roles
  ) {
    return Stream.of(CompositionContributionRole.values())
        .filter(roles::contains)
        .toList();
  }

  private static @NonNull CompositionContributionEvidence evidence(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal source,
      @NonNull BigDecimal target,
      @NonNull BigDecimal deviation,
      @NonNull CompositionContributionRole role
  ) {
    return new CompositionContributionEvidence(axis, source, target, deviation, role);
  }

  private static @NonNull BigDecimal valueOf(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalAxis axis
  ) {
    return axes.valueOf(axis).orElseThrow().value();
  }

  private static @Nullable BigDecimal nullableValueOf(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalAxis axis
  ) {
    final var value = axes.valueOf(axis).map(AxisValue::value);
    return value.orElse(null); // NOSONAR
  }

  @Override
  public @NonNull CompositionContributionRoleAssessment assess(
      @NonNull CompositionContributionRoleAssessmentRequest request
  ) {
    requireNonNull(request, "composition contribution assessment request cannot be null");

    final var axes = request.sourceAxes();
    final var target = request.targetObjective();
    final var missingAxes = missingRequiredAxes(axes);
    if (!missingAxes.isEmpty()) {
      return unassessable(missingAxes);
    }

    final var sugar = valueOf(axes, ChemicalAxis.SUGAR);
    final var acidity = valueOf(axes, ChemicalAxis.ACIDITY);
    final var sourcePoint = new SugarAcidityCoordinates(sugar, acidity);
    final var targetPoint = target.sugarAcidityTarget().geometry().nearestPointTo(sourcePoint);
    final var roles = EnumSet.noneOf(CompositionContributionRole.class);
    final var evidence = new ArrayList<CompositionContributionEvidence>();

    addSugarEvidence(sugar, targetPoint.sugar(), roles, evidence);
    addAcidityEvidence(acidity, targetPoint.acidity(), roles, evidence);
    addTanninEvidence(axes, target, roles, evidence, missingAxes);

    if (targetContains(sourcePoint, axes, target)) {
      roles.add(CompositionContributionRole.BALANCED_BASE);
    }
    if (isDilutionSource(sugar, targetPoint.sugar(), acidity, targetPoint.acidity())) {
      roles.add(CompositionContributionRole.DILUTION_SOURCE);
    }
    if (roles.isEmpty()) {
      roles.add(CompositionContributionRole.LOW_FIT_SOURCE);
    }

    final var orderedRoles = orderedRoles(roles);
    return new CompositionContributionRoleAssessment(
        orderedRoles,
        orderedRoles.getFirst(),
        evidence,
        missingAxes
    );
  }

  private enum TargetDeviation {
    BELOW_TARGET,
    MATCHES_TARGET,
    ABOVE_TARGET;

    private static TargetDeviation of(@NonNull BigDecimal deviationFromTarget) {
      return switch (deviationFromTarget.signum()) {
        case -1 -> BELOW_TARGET;
        case 0 -> MATCHES_TARGET;
        case 1 -> ABOVE_TARGET;
        default -> throw new DomainRuleViolationException("unexpected decimal signum");
      };
    }

    private boolean isAboveTarget() {
      return this == ABOVE_TARGET;
    }

    private boolean isBelowTarget() {
      return this == BELOW_TARGET;
    }
  }
}
