package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.craft.mcs.cap.shared.platform.query.OpaqueCursorCodec;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;


/**
 * Read-side use case for current feedstock batch points on the sugar-acidity graph.
 *
 * <p>The service applies the product rule that only the latest measurement of each batch represents
 * the current batch profile. It then evaluates capability and zone filters in the application
 * layer, returning cursor pages for workspace graph rendering.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchCurrentSugarAcidityZonePointsService
    implements QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  private static final int PAGE_LOOKAHEAD = 1;

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull CursorPage<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock batch current sugar-acidity zone point query cannot be null");

    final var batchesById = feedstockBatchRepository.findAllByCiderMakerId(query.ciderMakerId())
        .stream()
        .collect(toMap(FeedstockBatch::batchId, Function.identity()));

    return ChunkedCursorPageCollector.collect(
        decodeCursor(query.page().after()),
        query.page().limit(),
        PAGE_LOOKAHEAD,
        (afterCursor, limit) -> currentMeasurementQueryService.findCurrentSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
                query.ciderMakerId(),
                query.filters().batch(),
                query.filters().season(),
                afterCursor,
                limit
            )
        ),
        QueryFeedstockBatchCurrentSugarAcidityZonePointsService::toCandidateCursor,
        measurement -> toResultIfMatches(measurement, query, batchesById),
        QueryFeedstockBatchCurrentSugarAcidityZonePointsService::encodeCursor
    );
  }

  private @NonNull FeedstockBatchCurrentSugarAcidityZoneResult toResult(
      @NonNull FeedstockMeasurement measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull FeedstockBatch batch,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        measurement.batchId(),
        batch.name(),
        query.ciderMakerId(),
        measurement.measurementId(),
        measurement.measuredAt(),
        evaluation.plottable(),
        evaluation.assessment(),
        evaluation.capabilityAssessment()
    );
  }

  private @NonNull Optional<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurement measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById
  ) {
    final var batch = batchesById.get(measurement.batchId());
    if (batch == null) {
      return Optional.empty();
    }

    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(measurement, query, batch, evaluation);
    return matchesSugarAcidityZone(result, query.filters().sugarAcidityZone())
        ? Optional.of(result)
        : Optional.empty();
  }

  private boolean matchesCapability(
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }

    final var assessment = sugarAcidityProfileProjector.assessCapability(evaluation, criteria.capability());

    return assessment.capable() == Boolean.TRUE.equals(criteria.capable());
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    if (criteria == null) {
      return true;
    }

    final SugarAcidityZoneAssessment assessment = result.assessment();
    return assessment != null
        && criteria.zones().contains(assessment.zone());
  }

  private static @NonNull FeedstockMeasurementSugarAcidityPointCandidateCursor toCandidateCursor(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new FeedstockMeasurementSugarAcidityPointCandidateCursor(
        measurement.measuredAt(),
        measurement.measurementId().value()
    );
  }

  private static @Nullable FeedstockMeasurementSugarAcidityPointCandidateCursor decodeCursor(@Nullable String cursor) {
    if (cursor == null) {
      return null;
    }

    try {
      final var parts = OpaqueCursorCodec.decode(cursor, 2);
      return new FeedstockMeasurementSugarAcidityPointCandidateCursor(Instant.parse(parts.getFirst()), UUID.fromString(parts.get(1)));
    } catch (IllegalArgumentException exception) {
      throw new MalformedCursorException(exception);
    }
  }

  private static @NonNull String encodeCursor(@NonNull FeedstockMeasurementSugarAcidityPointCandidateCursor cursor) {
    return OpaqueCursorCodec.encode(cursor.measuredAt().toString(), cursor.measurementId().toString());
  }
}
