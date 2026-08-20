package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper;

import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.ProfileTargetCandidateCommonRestMapper.toAppleWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper.toAppleGraphCriteria;


public final class AppleVarietyProfileTargetCandidateRestMapper {

  private AppleVarietyProfileTargetCandidateRestMapper() {
  }

  public static @NonNull AppleVarietyProfileTargetCandidateQuery toQuery(
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  ) {
    requireNonNull(request, "apple variety profile target candidate query request cannot be null");
    final var requestedZone = request.zone();
    return new AppleVarietyProfileTargetCandidateQuery(
        new AppleVarietyAnalysisFilters(toTextCriteria(request.nameContains())),
        toAppleGraphCriteria(
            requestedZone == null
                ? null
                : org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone
                    .valueOf(requestedZone.name()), // NOSONAR
            null
        ),
        AppleTargetFitConfiguration.AnalysisScenario.valueOf(
            requireNonNull(request.analysisScenario(), "profile target analysis scenario cannot be null").name()
        ),
        toTarget(request),
        toAppleWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
    );
  }

  public static @NonNull AppleVarietyProfileTargetCandidatesResponse toResponse(
      @NonNull AppleVarietyProfileTargetCandidates candidates
  ) {
    requireNonNull(candidates, "apple variety profile target candidates cannot be null");
    return new AppleVarietyProfileTargetCandidatesResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetAnalysisScenario
            .valueOf(candidates.analysisScenario().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode
            .valueOf(candidates.normalizerCode().name()),
        candidates.candidates().stream()
            .map(AppleVarietyProfileTargetCandidateRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull AppleVarietyProfileTargetCandidateResponse toResponse(
      @NonNull AppleVarietyProfileTargetCandidate candidate
  ) {
    return new AppleVarietyProfileTargetCandidateResponse(
        candidate.varietyId().value(),
        candidate.varietyName().value(),
        ProfileTargetScoringCharacteristicsRestMapper.toResponse(candidate.characteristics()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        CompositionContributionRoleRestMapper.toResponse(candidate.contributionRole())
    );
  }

  private static @NonNull AppleTargetDefinition toTarget(
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  ) {
    final var sugarAcidity = switch (request.target()) {
      case PredefinedZoneProfileTargetRequest(_, var zone) -> new AppleTargetDefinition.PredefinedTarget(
          AppleSugarAcidityGraphView.Zone.valueOf(zone.name())
      );
      case CustomRectangleProfileTargetRequest rectangle -> new AppleTargetDefinition.CustomRectangleTarget(
          MeasuredValueRange.closed(
              SpecificGravityValue.of(rectangle.specificGravityMin()),
              SpecificGravityValue.of(rectangle.specificGravityMax())
          ),
          MeasuredValueRange.closed(
              TitratableAcidityValue.of(rectangle.titratableAcidityMin()),
              TitratableAcidityValue.of(rectangle.titratableAcidityMax())
          )
      );
    };
    final var tannin = request.tanninTarget();
    return new AppleTargetDefinition(
        sugarAcidity,
        tannin == null
            ? null
            : MeasuredValueRange.closed(TanninValue.of(tannin.min()), TanninValue.of(tannin.max()))
    );
  }

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }
}
