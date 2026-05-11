package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record BlendProfileObjective(
    @NonNull SugarAcidityTarget sugarAcidityTarget,
    @Nullable TanninTargetRange tanninTarget
) {

  public BlendProfileObjective {
    requireNonNull(sugarAcidityTarget, "sugar-acidity target cannot be null");
  }

  public static @NonNull BlendProfileObjective of(
      @NonNull SugarAcidityTarget target
  ) {
    return new BlendProfileObjective(target, null);
  }

  public static @NonNull BlendProfileObjective withTanninTarget(
      @NonNull SugarAcidityTarget target,
      @NonNull TanninTargetRange tanninTarget
  ) {
    return new BlendProfileObjective(
        target,
        requireNonNull(tanninTarget, "tannin target cannot be null")
    );
  }

  public boolean requiresTannin() {
    return tanninTarget != null;
  }
}
