package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidate;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyProfileTargetCandidates;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.AppleVarietyProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.FeedstockBatchProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.FeedstockBatchProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.FeedstockBatchProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ProfileTargetCandidateRestMapper {

  private ProfileTargetCandidateRestMapper() {
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
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        request.purpose(),
        requireNonNull(
            ProfileTargetRestMapper.toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
            "profile target objective cannot be null"
        ),
        toWeights(request.weights()),
        request.limit() == null ? CursorPageRequest.DEFAULT_LIMIT : request.limit()
    );
  }

  public static @NonNull FeedstockBatchProfileTargetCandidateQuery toFeedstockQuery(
      @NonNull FeedstockBatchProfileTargetCandidateSearchRequest request
  ) {
    requireNonNull(request, "feedstock batch profile target candidate query request cannot be null");
    return new FeedstockBatchProfileTargetCandidateQuery(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchIds()),
            toSeason(request.season()),
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        request.purpose(),
        toTarget(request.target()),
        request.tanninTarget() == null ? null : new TanninTargetRange(
            request.tanninTarget().min(), // NOSONAR
            request.tanninTarget().max() // NOSONAR
        ),
        toWeights(request.weights()),
        request.limit() == null ? CursorPageRequest.DEFAULT_LIMIT : request.limit()
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
            .map(ProfileTargetCandidateRestMapper::toResponse)
            .toList()
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
            .map(ProfileTargetCandidateRestMapper::toResponse)
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

  private static @NonNull FeedstockBatchProfileTargetCandidateResponse toResponse(
      @NonNull FeedstockBatchProfileTargetCandidate score
  ) {
    requireNonNull(score, "feedstock batch profile target score cannot be null");
    return new FeedstockBatchProfileTargetCandidateResponse(
        score.batchId().value(),
        score.currentMeasurementId().value(),
        score.currentMeasurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(score.point()),
        ProfileTargetRestMapper.toResponse(score.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(score.contributionRole()),
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

  private static @NonNull ProfileTargetObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return ProfileTargetObjectiveWeights.balanced();
    }

    final BigDecimal sugar = request.sugar() == null ? BigDecimal.ONE : request.sugar();
    final BigDecimal acidity = request.acidity() == null ? BigDecimal.ONE : request.acidity();
    return new ProfileTargetObjectiveWeights(sugar, acidity, request.tannin()); // NOSONAR
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
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

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }
}
