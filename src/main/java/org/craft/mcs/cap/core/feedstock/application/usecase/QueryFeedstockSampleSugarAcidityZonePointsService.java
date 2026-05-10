package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.QueryFeedstockSampleSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleSugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.shared.application.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.CursorPage;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.application.query.OpaqueCursorCodec;
import org.craft.mcs.cap.shared.application.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class QueryFeedstockSampleSugarAcidityZonePointsService
    implements QueryFeedstockSampleSugarAcidityZonePointsUseCase {

  private static final int PAGE_LOOKAHEAD = 1;

  private final @NonNull FeedstockSampleQueryRepository feedstockSampleQueryRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull CursorPage<@NonNull FeedstockSampleSugarAcidityZoneResult> query(
      @NonNull FeedstockSampleSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock sample sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        decodeCursor(query.page().after()),
        query.page().limit(),
        PAGE_LOOKAHEAD,
        (afterCursor, limit) -> feedstockSampleQueryRepository.findSugarAcidityPointCandidates(
            new FeedstockSampleSugarAcidityPointCandidateQuery(
                query.ciderMakerId(),
                query.filters().batch(),
                query.filters().season(),
                afterCursor,
                limit
            )
        ),
        QueryFeedstockSampleSugarAcidityZonePointsService::toCandidateCursor,
        sample -> toResultIfMatches(sample, query),
        QueryFeedstockSampleSugarAcidityZonePointsService::encodeCursor
    );
  }

  private @NonNull FeedstockSampleSugarAcidityZoneResult toResult(
      @NonNull FeedstockSample sample,
      @NonNull FeedstockSampleSugarAcidityZonePointQuery query
  ) {
    final var axes = axisExtractor.extract(sample.chemicalProfile());
    final var plotCapability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
    final var assessment = plotCapability.capable()
        ? assessor.assess(axes).orElse(null) // NOSONAR
        : null;

    return new FeedstockSampleSugarAcidityZoneResult(
        sample.sampleId(),
        query.ciderMakerId(),
        sample.batchId(),
        sample.name(),
        assessment != null,
        assessment,
        plotCapability
    );
  }

  private @NonNull Optional<@NonNull FeedstockSampleSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockSample sample,
      @NonNull FeedstockSampleSugarAcidityZonePointQuery query
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
      @NonNull FeedstockSample sample,
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
      @NonNull FeedstockSampleSugarAcidityZoneResult result,
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

  private static @NonNull FeedstockSampleSugarAcidityPointCandidateCursor toCandidateCursor(
      @NonNull FeedstockSample sample
  ) {
    requireNonNull(sample, "feedstock sample cannot be null");
    return new FeedstockSampleSugarAcidityPointCandidateCursor(
        sample.measuredAt(),
        sample.sampleId().value()
    );
  }

  private static @Nullable FeedstockSampleSugarAcidityPointCandidateCursor decodeCursor(@Nullable String cursor) {
    if (cursor == null) {
      return null;
    }

    try {
      final var parts = OpaqueCursorCodec.decode(cursor, 2);
      return new FeedstockSampleSugarAcidityPointCandidateCursor(Instant.parse(parts.getFirst()), UUID.fromString(parts.get(1)));
    } catch (IllegalArgumentException exception) {
      throw new MalformedCursorException(exception);
    }
  }

  private static @NonNull String encodeCursor(@NonNull FeedstockSampleSugarAcidityPointCandidateCursor cursor) {
    return OpaqueCursorCodec.encode(cursor.measuredAt().toString(), cursor.sampleId().toString());
  }
}
