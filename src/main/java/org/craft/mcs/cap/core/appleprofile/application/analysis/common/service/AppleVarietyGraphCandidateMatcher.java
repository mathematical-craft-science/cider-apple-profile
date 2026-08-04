package org.craft.mcs.cap.core.appleprofile.application.analysis.common.service;

import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Applies graph capability and zone filters to typed apple-variety reference characteristics. */
@Component
public final class AppleVarietyGraphCandidateMatcher {

  public boolean matches(
      @NonNull AppleVarietyReferenceProfile referenceProfile,
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull AppleVarietyAnalysisFilters filters
  ) {
    requireNonNull(referenceProfile, "apple variety reference profile cannot be null");
    requireNonNull(graphEvaluation, "apple variety graph evaluation cannot be null");
    requireNonNull(filters, "apple variety analysis filters cannot be null");
    return matchesCapability(referenceProfile, filters)
        && matchesZone(graphEvaluation, filters);
  }

  private static boolean matchesCapability(
      @NonNull AppleVarietyReferenceProfile referenceProfile,
      @NonNull AppleVarietyAnalysisFilters filters
  ) {
    final var criteria = filters.capability();
    if (criteria == null) {
      return true;
    }
    return criteria.matches(new ProfileAnalysisInputAvailability(
        referenceProfile.specificGravity() != null,
        referenceProfile.titratableAcidity() != null,
        referenceProfile.tannin() != null
    ));
  }

  private static boolean matchesZone(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull AppleVarietyAnalysisFilters filters
  ) {
    final var criteria = filters.sugarAcidityZone();
    if (criteria == null) {
      return true;
    }
    final var zoneAssessment = graphEvaluation.zoneAssessment();
    return zoneAssessment != null && criteria.matches(zoneAssessment);
  }
}
