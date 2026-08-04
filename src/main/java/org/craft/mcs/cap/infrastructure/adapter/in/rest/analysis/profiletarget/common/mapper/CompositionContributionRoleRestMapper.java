package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper;

import java.util.Comparator;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionEvidence;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
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
        assessment.evidence().stream()
            .map(CompositionContributionRoleRestMapper::toResponse)
            .toList(),
        assessment.missingAxes().stream()
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  private static @NonNull CompositionContributionEvidenceResponse toResponse(
      @NonNull CompositionContributionEvidence evidence
  ) {
    requireNonNull(evidence, "composition contribution evidence cannot be null");
    final var unit = CanonicalUnitLabels.forAxis(evidence.axis());
    return new CompositionContributionEvidenceResponse(
        evidence.axis(),
        new DecimalQuantityResponse(evidence.sourceValue().asBigDecimal(), unit),
        new DecimalQuantityResponse(evidence.targetCoordinate().asBigDecimal(), unit),
        new DecimalQuantityResponse(evidence.deviationFromTarget(), unit),
        evidence.direction(),
        evidence.effect(),
        evidence.severity()
    );
  }
}
