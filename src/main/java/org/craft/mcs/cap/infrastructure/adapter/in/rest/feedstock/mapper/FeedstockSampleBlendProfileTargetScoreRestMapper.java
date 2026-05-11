package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockSampleBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.BlendProfileObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.BlendProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CustomRectangleFeedstockSampleBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleBlendProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleBlendProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PredefinedZoneFeedstockSampleBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockSampleBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.TanninTargetRangeRequest;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockSampleBlendProfileTargetScoreRestMapper {

  private FeedstockSampleBlendProfileTargetScoreRestMapper() {
  }

  public static @NonNull ScoreFeedstockSampleBlendProfileTargetCommand toCommand(
      @NonNull ScoreFeedstockSampleBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock sample blend profile target request cannot be null");

    return new ScoreFeedstockSampleBlendProfileTargetCommand(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockSamplePointFilters(
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

  public static @NonNull FeedstockSampleBlendProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockSampleBlendProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock sample blend profile target scores cannot be null");

    return new FeedstockSampleBlendProfileTargetScoreBatchResponse(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(FeedstockSampleBlendProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockSampleBlendProfileTargetScoreResponse toResponse(
      @NonNull FeedstockSampleBlendProfileTargetScore score
  ) {
    requireNonNull(score, "feedstock sample blend profile target score cannot be null");

    return new FeedstockSampleBlendProfileTargetScoreResponse(
        score.sampleId().value(),
        score.ciderMakerId().value(),
        score.batchId().value(),
        score.sampleName().value(),
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

  private static @NonNull FeedstockSampleBlendProfileTargetSelection toTarget(
      @NonNull FeedstockSampleBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock sample blend profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneFeedstockSampleBlendProfileTargetRequest predefined ->
          new PredefinedFeedstockSampleBlendProfileTargetSelection(predefined.zone());
      case CustomRectangleFeedstockSampleBlendProfileTargetRequest custom ->
          new CustomRectangleFeedstockSampleBlendProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
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
