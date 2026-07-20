package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietySugarAcidityPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
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

  private final @NonNull AppleVarietySugarAcidityPointCandidateRepository candidateRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull CursorPage<
      @NonNull AppleVarietySugarAcidityZoneResult,
      @NonNull AppleVarietySugarAcidityPagePosition
  > query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> candidateRepository.findCandidates(
            new AppleVarietySugarAcidityPointCandidateQuery(
                query.filters().nameContains(),
                afterPosition,
                limit
            )
        ),
        QueryAppleVarietySugarAcidityZonePointsService::toPagePosition,
        variety -> toResultIfMatches(variety, query)
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

    return assessment.capable() == criteria.capable(); // NOSONAR
  }

  private @NonNull AppleVarietySugarAcidityZoneResult toResult(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        evaluation,
        potentialAbvPredictor.predictIfSugarPresent(evaluation.axes()).orElse(null) // NOSONAR
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

  private static @NonNull AppleVarietySugarAcidityPagePosition toPagePosition(
      @NonNull AppleVarietySugarAcidityPointCandidateView variety
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    return new AppleVarietySugarAcidityPagePosition(
        variety.name().value(),
        variety.varietyId().value()
    );
  }
}
