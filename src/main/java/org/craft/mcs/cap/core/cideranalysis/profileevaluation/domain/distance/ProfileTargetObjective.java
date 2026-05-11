package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ProfileTargetObjective(
    @NonNull SugarAcidityTarget sugarAcidityTarget,
    @Nullable TanninTargetRange tanninTarget
) {

  public ProfileTargetObjective {
    requireNonNull(sugarAcidityTarget, "sugar-acidity target cannot be null");
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
}
