package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateCursor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.craft.mcs.cap.shared.platform.query.OpaqueCursorCodec;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side use case for catalog variety points on the sugar-acidity graph.
 *
 * <p>The service performs cursor pagination over repository candidates, applies application-level
 * capability and zone filters, and returns both plottable and unplottable results according to the
 * requested filters. It keeps graph read queries out of the aggregate repository contract and makes
 * catalog exploration independent from feedstock batch queries.
 */
@Component
@RequiredArgsConstructor
public final class QueryAppleVarietySugarAcidityZonePointsService
    implements QueryAppleVarietySugarAcidityZonePointsUseCase {

  private static final int PAGE_LOOKAHEAD = 1;

  private final @NonNull AppleVarietySugarAcidityPointCandidateRepository candidateRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull CursorPage<@NonNull AppleVarietySugarAcidityZoneResult> query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        decodeCursor(query.page().after()),
        query.page().limit(),
        PAGE_LOOKAHEAD,
        (afterCursor, limit) -> candidateRepository.findCandidates(
            new AppleVarietySugarAcidityPointCandidateQuery(
                query.filters().nameContains(),
                afterCursor,
                limit
            )
        ),
        QueryAppleVarietySugarAcidityZonePointsService::toCandidateCursor,
        variety -> toResultIfMatches(variety, query),
        QueryAppleVarietySugarAcidityZonePointsService::encodeCursor
    );
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

  private static @NonNull AppleVarietySugarAcidityZoneResult toResult(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        evaluation
    );
  }

  private @NonNull Optional<@NonNull AppleVarietySugarAcidityZoneResult> toResultIfMatches(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    final var evaluation = sugarAcidityProfileProjector.project(variety.chemicalProfile());
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(variety, evaluation);
    return matchesSugarAcidityZone(result, query.filters().sugarAcidityZone())
        ? Optional.of(result)
        : Optional.empty();
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull AppleVarietySugarAcidityZoneResult result,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    if (criteria == null) {
      return true;
    }

    final SugarAcidityZoneAssessment assessment = result.evaluation().assessment();
    return assessment != null
        && criteria.zones().contains(assessment.zone());
  }

  private static @NonNull AppleVarietySugarAcidityPointCandidateCursor toCandidateCursor(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    return new AppleVarietySugarAcidityPointCandidateCursor(
        variety.name().value(),
        variety.varietyId().value()
    );
  }

  private static @Nullable AppleVarietySugarAcidityPointCandidateCursor decodeCursor(@Nullable String cursor) {
    if (cursor == null) {
      return null;
    }

    try {
      final var parts = OpaqueCursorCodec.decode(cursor, 2);
      return new AppleVarietySugarAcidityPointCandidateCursor(parts.getFirst(), UUID.fromString(parts.get(1)));
    } catch (IllegalArgumentException exception) {
      throw new MalformedCursorException(exception);
    }
  }

  private static @NonNull String encodeCursor(@NonNull AppleVarietySugarAcidityPointCandidateCursor cursor) {
    return OpaqueCursorCodec.encode(cursor.name(), cursor.varietyId().toString());
  }
}
