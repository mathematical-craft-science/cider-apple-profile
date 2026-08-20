package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper;

import java.util.Comparator;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView.Evidence;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionEvidenceDirection;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionEvidenceEffect;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionEvidenceSeverity;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRole;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class CompositionContributionRoleRestMapper {

  private CompositionContributionRoleRestMapper() {
  }

  public static @NonNull CompositionContributionRoleAssessmentResponse toResponse(
      @NonNull FeedstockContributionView assessment
  ) {
    requireNonNull(assessment, "feedstock contribution assessment cannot be null");
    return new CompositionContributionRoleAssessmentResponse(
        assessment.roles().stream()
            .map(role -> CompositionContributionRole.valueOf(role.name()))
            .toList(),
        assessment.evidence().stream()
            .map(CompositionContributionRoleRestMapper::toResponse)
            .toList(),
        assessment.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  public static @NonNull CompositionContributionRoleAssessmentResponse toResponse(
      @NonNull AppleContributionView assessment
  ) {
    requireNonNull(assessment, "apple contribution assessment cannot be null");
    return new CompositionContributionRoleAssessmentResponse(
        assessment.roles().stream()
            .map(role -> CompositionContributionRole.valueOf(role.name()))
            .toList(),
        assessment.evidence().stream()
            .map(CompositionContributionRoleRestMapper::toResponse)
            .toList(),
        assessment.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  private static @NonNull CompositionContributionEvidenceResponse toResponse(
      @NonNull Evidence evidence
  ) {
    requireNonNull(evidence, "feedstock contribution evidence cannot be null");
    final var axis = org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
        .valueOf(evidence.axis().name());
    final var unit = CanonicalUnitLabels.forAxis(axis);
    return new CompositionContributionEvidenceResponse(
        axis,
        new DecimalQuantityResponse(evidence.sourceValue(), unit),
        new DecimalQuantityResponse(evidence.targetCoordinate(), unit),
        new DecimalQuantityResponse(evidence.deviationFromTarget(), unit),
        CompositionContributionEvidenceDirection.valueOf(evidence.direction().name()),
        CompositionContributionEvidenceEffect.valueOf(evidence.effect().name()),
        CompositionContributionEvidenceSeverity.valueOf(evidence.severity().name())
    );
  }

  private static @NonNull CompositionContributionEvidenceResponse toResponse(
      AppleContributionView.@NonNull Evidence evidence
  ) {
    final var axis = org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
        .valueOf(evidence.axis().name());
    final var unit = CanonicalUnitLabels.forAxis(axis);
    return new CompositionContributionEvidenceResponse(
        axis,
        new DecimalQuantityResponse(evidence.sourceValue(), unit),
        new DecimalQuantityResponse(evidence.targetCoordinate(), unit),
        new DecimalQuantityResponse(evidence.deviationFromTarget(), unit),
        CompositionContributionEvidenceDirection.valueOf(evidence.direction().name()),
        CompositionContributionEvidenceEffect.valueOf(evidence.effect().name()),
        CompositionContributionEvidenceSeverity.valueOf(evidence.severity().name())
    );
  }
}
