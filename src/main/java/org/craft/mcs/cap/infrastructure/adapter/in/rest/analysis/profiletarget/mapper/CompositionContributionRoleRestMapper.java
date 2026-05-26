package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.mapper;

import java.util.Comparator;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionEvidence;
import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.CompositionContributionEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class CompositionContributionRoleRestMapper {

  private CompositionContributionRoleRestMapper() {
  }

  public static @Nullable CompositionContributionRoleAssessmentResponse toResponse(
      @Nullable CompositionContributionRoleAssessment assessment
  ) {
    if (assessment == null) {
      return null;
    }

    return new CompositionContributionRoleAssessmentResponse(
        assessment.roles(),
        assessment.primaryRole(),
        assessment.evidence().stream()
            .map(CompositionContributionRoleRestMapper::toResponse)
            .toList(),
        assessment.missingAxes().stream()
            .sorted(Comparator.comparing(ChemicalAxis::name))
            .toList(),
        assessment.assessable()
    );
  }

  private static @NonNull CompositionContributionEvidenceResponse toResponse(
      @NonNull CompositionContributionEvidence evidence
  ) {
    requireNonNull(evidence, "composition contribution evidence cannot be null");
    final var unit = CanonicalUnitLabels.forAxis(evidence.axis());
    return new CompositionContributionEvidenceResponse(
        evidence.axis(),
        new DecimalQuantityResponse(evidence.sourceValue(), unit),
        new DecimalQuantityResponse(evidence.targetCoordinate(), unit),
        new DecimalQuantityResponse(evidence.deviationFromTarget(), unit),
        evidence.role()
    );
  }
}
