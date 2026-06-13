package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.ProfileTargetCandidateCommonRestMapper.toWeights;


public final class FeedstockBatchProfileTargetCandidateRestMapper {

  private FeedstockBatchProfileTargetCandidateRestMapper() {
  }

  public static @NonNull FeedstockBatchProfileTargetCandidateQuery toQuery(
      @NonNull FeedstockBatchProfileTargetCandidateSearchRequest request
  ) {
    requireNonNull(request, "feedstock batch profile target candidate query request cannot be null");

    return new FeedstockBatchProfileTargetCandidateQuery(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchIds()),
            toSeason(request.season()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        requireNonNull(request.purpose(), "profile target scoring purpose cannot be null"),
        toTarget(request.target()),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        requireNonNullElse(request.limit(), CursorPageRequest.DEFAULT_LIMIT)
    );
  }

  public static @NonNull FeedstockBatchProfileTargetCandidatesResponse toResponse(
      @NonNull FeedstockBatchProfileTargetCandidates candidates
  ) {
    requireNonNull(candidates, "feedstock batch profile target candidates cannot be null");

    return new FeedstockBatchProfileTargetCandidatesResponse(
        candidates.purpose(),
        candidates.normalizerCode(),
        candidates.candidates().stream()
            .map(FeedstockBatchProfileTargetCandidateRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchProfileTargetCandidateResponse toResponse(
      @NonNull FeedstockBatchProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "feedstock batch profile target candidate cannot be null");

    return new FeedstockBatchProfileTargetCandidateResponse(
        candidate.batchId().value(),
        candidate.currentMeasurementId().value(),
        candidate.currentMeasurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(candidate.point()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(candidate.contributionRole()),
            "composition contribution role response cannot be null"
        )
    );
  }

  private static @NonNull FeedstockProfileTargetSelection toTarget(@NonNull ProfileTargetRequest request) {
    requireNonNull(request, "feedstock profile target request cannot be null");

    return switch (request) {
      case PredefinedZoneProfileTargetRequest predefined ->
          new PredefinedFeedstockProfileTargetSelection(predefined.zone());
      case CustomRectangleProfileTargetRequest custom ->
          new CustomRectangleFeedstockProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          ));
    };
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
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

  private static @Nullable TanninTargetRange toTanninTarget(@Nullable ProfileTargetTanninRangeRequest request) {
    return request == null ? null : new TanninTargetRange(request.min(), request.max());
  }

  private static @Nullable FeedstockBatchCriteria toBatchCriteria(
      @Nullable List<@NonNull UUID> batchIds
  ) {
    if (batchIds == null || batchIds.isEmpty()) {
      return null;
    }

    return new FeedstockBatchCriteria(
        batchIds.stream()
            .map(FeedstockBatchId::of)
            .collect(Collectors.toSet())
    );
  }
}
