package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper;

import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.ProfileTargetCandidateCommonRestMapper.toWeights;


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
        new AppleVarietyPointFilters(
            toTextCriteria(request.nameContains()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        requireNonNull(request.purpose(), "profile target scoring purpose cannot be null"),
        requireNonNull(
            ProfileTargetRestMapper.toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
            "profile target objective cannot be null"
        ),
        toWeights(request.weights()),
        requireNonNullElse(request.limit(), CursorPageRequest.DEFAULT_LIMIT)
    );
  }

  public static @NonNull AppleVarietyProfileTargetCandidatesResponse toResponse(
      @NonNull AppleVarietyProfileTargetCandidates candidates
  ) {
    requireNonNull(candidates, "apple variety profile target candidates cannot be null");

    return new AppleVarietyProfileTargetCandidatesResponse(
        candidates.purpose(),
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
        SugarAcidityZoneRestMapper.toResponse(candidate.point()),
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

  private static @Nullable SugarAcidityZoneCriteria toZoneCriteria(@Nullable SugarAcidityZone zone) {
    return zone == null ? null : SugarAcidityZoneCriteria.of(zone);
  }

  private static @Nullable ChemicalProfileCapabilityCriteria toCapabilityCriteria(
      @Nullable ChemicalProfileCapability capability,
      @Nullable Boolean capable
  ) {
    return capability == null ? null : new ChemicalProfileCapabilityCriteria(capability, capable);
  }
}
