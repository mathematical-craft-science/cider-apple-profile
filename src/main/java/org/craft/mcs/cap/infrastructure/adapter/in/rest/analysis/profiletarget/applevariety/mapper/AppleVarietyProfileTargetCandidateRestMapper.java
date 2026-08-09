package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper;

import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.ProfileTargetCandidateCommonRestMapper.toWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper.toZoneCriteria;


public final class AppleVarietyProfileTargetCandidateRestMapper {

  private AppleVarietyProfileTargetCandidateRestMapper() {
  }

  public static @NonNull AppleVarietyProfileTargetCandidateQuery toQuery(
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "apple variety profile target candidate query request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    return new AppleVarietyProfileTargetCandidateQuery(
        new AppleVarietyAnalysisFilters(toTextCriteria(request.nameContains())),
        toZoneCriteria(request.zone()),
        requireNonNull(request.analysisScenario(), "profile target analysis scenario cannot be null"),
        requireNonNull(
            ProfileTargetRestMapper.toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
            "profile target objective cannot be null"
        ),
        toWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
    );
  }

  public static @NonNull AppleVarietyProfileTargetCandidatesResponse toResponse(
      @NonNull AppleVarietyProfileTargetCandidates candidates
  ) {
    requireNonNull(candidates, "apple variety profile target candidates cannot be null");

    return new AppleVarietyProfileTargetCandidatesResponse(
        candidates.analysisScenario(),
        candidates.normalizerCode(),
        candidates.candidates().stream()
            .map(AppleVarietyProfileTargetCandidateRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull AppleVarietyProfileTargetCandidateResponse toResponse(
      @NonNull AppleVarietyProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "apple variety profile target candidate cannot be null");

    return new AppleVarietyProfileTargetCandidateResponse(
        candidate.varietyId().value(),
        candidate.varietyName().value(),
        ProfileTargetScoringCharacteristicsRestMapper.toResponse(candidate.characteristics()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(candidate.contributionRole()),
            "composition contribution role response cannot be null"
        )
    );
  }

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }

}
