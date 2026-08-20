package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective;

import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetScoringInput;
import org.jspecify.annotations.NonNull;


/**
 * Scores a profile point against a configured target.
 *
 * <p>An objective function combines target geometry, optional tannin target, axis normalizer and
 * per-axis weights into one {@link ProfileTargetDistance}. It does not decide which normalizer to
 * use; that is done by application policy or request configuration before the objective function is
 * created by {@link ProfileTargetObjectiveFunctionFactory}.
 */
public interface ProfileTargetObjectiveFunction {

  @NonNull
  ProfileTargetObjectiveFunctionCode code();

  @NonNull
  ProfileTargetDistance distanceTo(
      @NonNull ProfileTargetScoringInput input,
      @NonNull ProfileTargetIntervalPolicy intervalPolicy,
      @NonNull ProfileTargetObjective objective
  );
}
