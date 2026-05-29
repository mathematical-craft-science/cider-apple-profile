package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockMeasurementSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
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


/**
 * Read-side use case for historical feedstock measurement points on the sugar-acidity graph.
 *
 * <p>This query includes all matching measurements, not only the latest per batch. It is useful for
 * audit/history views and for understanding how a batch profile evolved over time. Current
 * decision-support flows should use {@link QueryFeedstockBatchCurrentSugarAcidityZonePointsService}
 * instead.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockMeasurementSugarAcidityZonePointsService
    implements QueryFeedstockMeasurementSugarAcidityZonePointsUseCase {

  private static final int PAGE_LOOKAHEAD = 1;

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull CursorPage<@NonNull FeedstockMeasurementSugarAcidityZoneResult> query(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock measurement sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        decodeCursor(query.page().after()),
        query.page().limit(),
        PAGE_LOOKAHEAD,
        (afterCursor, limit) -> feedstockMeasurementQueryRepository.findSugarAcidityPointCandidates(
            new FeedstockMeasurementSugarAcidityPointCandidateQuery(
                query.ciderMakerId(),
                query.filters().batch(),
                query.filters().season(),
                afterCursor,
                limit
            )
        ),
        QueryFeedstockMeasurementSugarAcidityZonePointsService::toCandidateCursor,
        sample -> toResultIfMatches(sample, query),
        QueryFeedstockMeasurementSugarAcidityZonePointsService::encodeCursor
    );
  }

  private @NonNull FeedstockMeasurementSugarAcidityZoneResult toResult(
      @NonNull FeedstockMeasurement sample,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return new FeedstockMeasurementSugarAcidityZoneResult(
        sample.measurementId(),
        query.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        evaluation.plottable(),
        evaluation.assessment(),
        evaluation.capabilityAssessment()
    );
  }

  private @NonNull Optional<@NonNull FeedstockMeasurementSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurement sample,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    final var evaluation = sugarAcidityProfileProjector.project(sample.chemicalProfile());
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(sample, query, evaluation);
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
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result,
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
      @NonNull FeedstockMeasurement sample
  ) {
    requireNonNull(sample, "feedstock measurement cannot be null");
    return new FeedstockMeasurementSugarAcidityPointCandidateCursor(
        sample.measuredAt(),
        sample.measurementId().value()
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
