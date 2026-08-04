package org.craft.mcs.cap.core.feedstock.application.analysis.common.service;

import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Applies graph and transitional capability filters to typed current Feedstock characteristics.
 */
@Component
public final class FeedstockGraphCandidateMatcher {

  public boolean matches(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull FeedstockAnalysisCandidateFilters filters
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    return matches(
        new ProfileAnalysisInputAvailability(
            currentProfile.specificGravity() != null,
            currentProfile.titratableAcidity() != null,
            currentProfile.tannin() != null
        ),
        graphEvaluation,
        filters
    );
  }

  public boolean matches(
      @NonNull ProfileAnalysisInputAvailability availability,
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull FeedstockAnalysisCandidateFilters filters
  ) {
    requireNonNull(availability, "profile analysis input availability cannot be null");
    requireNonNull(graphEvaluation, "feedstock graph evaluation cannot be null");
    requireNonNull(filters, "feedstock analysis candidate filters cannot be null");
    return matchesCapability(availability, filters)
        && matchesZone(graphEvaluation, filters);
  }

  private static boolean matchesCapability(
      @NonNull ProfileAnalysisInputAvailability availability,
      @NonNull FeedstockAnalysisCandidateFilters filters
  ) {
    final var criteria = filters.capability();
    if (criteria == null) {
      return true;
    }
    return criteria.matches(availability);
  }

  private static boolean matchesZone(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull FeedstockAnalysisCandidateFilters filters
  ) {
    final var criteria = filters.sugarAcidityZone();
    if (criteria == null) {
      return true;
    }
    final var zoneAssessment = graphEvaluation.zoneAssessment();
    return zoneAssessment != null && criteria.matches(zoneAssessment);
  }
}
