package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.QueryFeedstockMeasurementSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.craft.mcs.cap.shared.platform.query.OpaqueCursorCodec;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
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
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

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
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var plotCapability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
    final var assessment = plotCapability.capable()
        ? assessor.assess(axes).orElse(null) // NOSONAR
        : null;

    return new FeedstockMeasurementSugarAcidityZoneResult(
        sample.measurementId(),
        query.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        assessment != null,
        assessment,
        plotCapability
    );
  }

  private @NonNull Optional<@NonNull FeedstockMeasurementSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurement sample,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    if (!matchesCapability(sample, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(sample, query);
    return matchesSugarAcidityZone(result, query.filters().sugarAcidityZone())
        ? Optional.of(result)
        : Optional.empty();
  }

  private boolean matchesCapability(
      @NonNull FeedstockMeasurement sample,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }

    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var assessment = capabilityAssessor.assess(axes, criteria.capability());

    return assessment.capable() == criteria.capable(); // NOSONAR
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
        && assessment.zone() != null
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
