package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.BlendProfileObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.BlendProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CustomRectangleFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PredefinedZoneFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.TanninTargetRangeRequest;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchBlendProfileTargetScoreRestMapper {

  private FeedstockBatchBlendProfileTargetScoreRestMapper() {
  }

  public static @NonNull ScoreFeedstockBatchBlendProfileTargetCommand toCommand(
      @NonNull ScoreFeedstockBatchBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock batch blend profile target request cannot be null");

    return new ScoreFeedstockBatchBlendProfileTargetCommand(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            request.batchId() == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(request.batchId())), // NOSONAR
            toSeason(request.season()),
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        request.purpose(),
        toTarget(request.target()),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        request.limit() == null ? CursorPageRequest.DEFAULT_LIMIT : request.limit()
    );
  }

  public static @NonNull FeedstockBatchBlendProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockBatchBlendProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock batch blend profile target scores cannot be null");

    return new FeedstockBatchBlendProfileTargetScoreBatchResponse(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(FeedstockBatchBlendProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchBlendProfileTargetScoreResponse toResponse(
      @NonNull FeedstockBatchBlendProfileTargetScore score
  ) {
    requireNonNull(score, "feedstock batch blend profile target score cannot be null");

    return new FeedstockBatchBlendProfileTargetScoreResponse(
        score.batchId().value(),
        score.ciderMakerId().value(),
        score.currentMeasurementId().value(),
        score.currentMeasurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(score.point()),
        toResponse(score.distance())
    );
  }

  private static @NonNull BlendProfileTargetDistanceResponse toResponse(
      @NonNull BlendProfileTargetDistance distance
  ) {
    requireNonNull(distance, "blend profile target distance cannot be null");

    return new BlendProfileTargetDistanceResponse(
        distance.status(),
        distance.value(),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        toResponse(distance.nearestTargetPoint()),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.objectiveFunction(),
        distance.metric(),
        distance.targetKind(),
        distance.targetShape(),
        distance.missingMeasurements()
    );
  }

  private static @NonNull SugarAcidityCoordinatesResponse toResponse(
      @NonNull SugarAcidityCoordinates coordinates
  ) {
    requireNonNull(coordinates, "sugar-acidity coordinates cannot be null");
    return new SugarAcidityCoordinatesResponse(
        coordinates.sugar(),
        coordinates.acidity()
    );
  }

  private static @NonNull FeedstockBatchBlendProfileTargetSelection toTarget(
      @NonNull FeedstockBatchBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock batch blend profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneFeedstockBatchBlendProfileTargetRequest predefined ->
          new PredefinedFeedstockBatchBlendProfileTargetSelection(predefined.zone());
      case CustomRectangleFeedstockBatchBlendProfileTargetRequest custom ->
          new CustomRectangleFeedstockBatchBlendProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          ));
    };
  }

  private static @Nullable TanninTargetRange toTanninTarget(
      @Nullable TanninTargetRangeRequest request
  ) {
    return request == null ? null : new TanninTargetRange(request.min(), request.max());
  }

  private static @NonNull BlendProfileObjectiveWeights toWeights(
      @Nullable BlendProfileObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return BlendProfileObjectiveWeights.balanced();
    }

    final BigDecimal sugar = request.sugar() == null ? BigDecimal.ONE : request.sugar();
    final BigDecimal acidity = request.acidity() == null ? BigDecimal.ONE : request.acidity();
    return new BlendProfileObjectiveWeights(sugar, acidity, request.tannin()); // NOSONAR
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }
}
