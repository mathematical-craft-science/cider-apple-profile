package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityPointDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;


/**
 * Target definition used by profile distance and optimization.
 *
 * <p>The two-dimensional SG/TA target is mandatory. Tannin is optional; when present,
 * objective functions require candidate tannin and return an unscorable distance if it is missing.
 */
public record ProfileTargetObjective(
    @NonNull SugarAcidityTarget sugarAcidityTarget,
    @Nullable TanninTargetRange tanninTarget
) {

  public ProfileTargetObjective {
    requireNonNull(sugarAcidityTarget, "SG/TA target cannot be null");
  }

  public static @NonNull ProfileTargetObjective of(
      @NonNull SugarAcidityTarget target
  ) {
    return new ProfileTargetObjective(target, null);
  }

  public static @NonNull ProfileTargetObjective withTanninTarget(
      @NonNull SugarAcidityTarget target,
      @NonNull TanninTargetRange tanninTarget
  ) {
    return new ProfileTargetObjective(
        target,
        requireNonNull(tanninTarget, "tannin target cannot be null")
    );
  }

  public boolean requiresTannin() {
    return tanninTarget != null;
  }

  /** Compares one representative profile point using the caller's authoritative distance policy. */
  public @NonNull ProfileTargetComparison compare(
      @NonNull ProfileTargetScoringPoint source,
      @NonNull SugarAcidityPointDistance distance
  ) {
    requireNonNull(source, "profile target source point cannot be null");
    requireNonNull(distance, "SG/TA point distance cannot be null");
    final var point = source.sugarAcidityPoint();
    final var geometry = sugarAcidityTarget.geometry();
    final var insideSugarAcidityTarget = geometry.contains(point);
    final var nearestTargetPoint = insideSugarAcidityTarget
        ? point
        : geometry.nearestPointTo(point, distance);

    if (tanninTarget == null) {
      return new ProfileTargetComparison(
          point,
          nearestTargetPoint,
          null,
          null,
          insideSugarAcidityTarget,
          insideSugarAcidityTarget,
          Set.of()
      );
    }

    final var tannin = source.tannin();
    if (tannin == null) {
      return new ProfileTargetComparison(
          point,
          nearestTargetPoint,
          null,
          null,
          insideSugarAcidityTarget,
          false,
          Set.of(ProfileEvaluationAxis.TANNIN)
      );
    }

    final var nearestTargetTannin = tanninTarget.nearestValueTo(tannin);
    return new ProfileTargetComparison(
        point,
        nearestTargetPoint,
        tannin,
        nearestTargetTannin,
        insideSugarAcidityTarget,
        insideSugarAcidityTarget && tanninTarget.contains(tannin),
        Set.of()
    );
  }
}
