package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.craft.mcs.cap.shared.platform.query.OpaqueCursorCodec;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


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

  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull CursorPage<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock batch current sugar-acidity zone point query cannot be null");

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
        measurement -> toResultIfMatches(measurement, query),
        QueryFeedstockBatchCurrentSugarAcidityZonePointsService::encodeCursor
    );
  }

  private @NonNull FeedstockBatchCurrentSugarAcidityZoneResult toResult(
      @NonNull FeedstockMeasurement measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    final var axes = axisExtractor.extract(measurement.chemicalProfile());
    final var plotCapability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
    final var assessment = plotCapability.capable()
        ? assessor.assess(axes).orElse(null) // NOSONAR
        : null;

    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        measurement.batchId(),
        query.ciderMakerId(),
        measurement.measurementId(),
        measurement.name(),
        assessment != null,
        assessment,
        plotCapability
    );
  }

  private @NonNull Optional<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurement measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    if (!matchesCapability(measurement, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(measurement, query);
    return matchesSugarAcidityZone(result, query.filters().sugarAcidityZone())
        ? Optional.of(result)
        : Optional.empty();
  }

  private boolean matchesCapability(
      @NonNull FeedstockMeasurement measurement,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }

    final var axes = axisExtractor.extract(measurement.chemicalProfile());
    final var assessment = capabilityAssessor.assess(axes, criteria.capability());

    return assessment.capable() == criteria.capable(); // NOSONAR
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
        && assessment.zone() != null
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
