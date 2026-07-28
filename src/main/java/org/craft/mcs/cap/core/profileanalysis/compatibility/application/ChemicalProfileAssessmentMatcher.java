package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.application.model.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Matches transitional ChemicalProfile assessments against reusable application query criteria. */
public final class ChemicalProfileAssessmentMatcher {

  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;

  public ChemicalProfileAssessmentMatcher(
      @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor
  ) {
    this.capabilityAssessor = requireNonNull(
        capabilityAssessor,
        "chemical profile capability assessor cannot be null"
    );
  }

  public boolean matches(
      @NonNull ChemicalProfileAssessment assessment,
      @Nullable ChemicalProfileCapabilityCriteria capabilityCriteria,
      @Nullable JolicoeurSugarAcidityZoneCriteria zoneCriteria
  ) {
    requireNonNull(assessment, "chemical profile assessment cannot be null");
    return matchesCapability(assessment, capabilityCriteria)
        && matchesZone(assessment, zoneCriteria);
  }

  private boolean matchesCapability(
      @NonNull ChemicalProfileAssessment assessment,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    return criteria == null || criteria.matches(
        capabilityAssessor.assess(assessment.axes(), criteria.capability()).capable()
    );
  }

  private static boolean matchesZone(
      @NonNull ChemicalProfileAssessment assessment,
      @Nullable JolicoeurSugarAcidityZoneCriteria criteria
  ) {
    if (criteria == null) {
      return true;
    }
    final var zoneAssessment = assessment.assessment();
    return zoneAssessment != null && criteria.matches(zoneAssessment);
  }
}
