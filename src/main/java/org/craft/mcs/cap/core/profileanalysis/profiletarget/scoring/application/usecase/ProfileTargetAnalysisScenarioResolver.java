package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;


/**
 * Resolves a user-facing analysis scenario to explicit mathematical scoring defaults.
 */
public interface ProfileTargetAnalysisScenarioResolver {

  @NonNull
  ProfileTargetScoringConfiguration resolve(
      @NonNull ProfileTargetAnalysisScenario scenario,
      @NonNull ProfileTargetObjectiveWeights weights
  );
}
