package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphCriteria;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidateQuery(
    @NonNull AppleVarietyAnalysisFilters filters,
    @Nullable AppleSugarAcidityGraphCriteria sourceZoneCriteria,
    @NonNull AnalysisScenario analysisScenario,
    @NonNull AppleTargetDefinition target,
    @NonNull ObjectiveWeights weights,
    int limit
) {

  public AppleVarietyProfileTargetCandidateQuery {
    requireNonNull(filters, "apple variety profile target candidate filters cannot be null");
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(target, "profile target objective cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "apple variety profile target candidate limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
