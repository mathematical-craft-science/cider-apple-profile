package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateCriteria;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.application.compatibility.ChemicalProfileAxesPotentialAbvAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;
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

  private final @NonNull AppleVarietyAnalysisCandidateQuery candidateQuery;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull CursorPage<
      @NonNull AppleVarietySugarAcidityZoneResult,
      @NonNull AppleVarietyCatalogPagePosition
  > query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> candidateQuery.findCandidates(
            new AppleVarietyAnalysisCandidateCriteria(
                query.filters().nameContains(),
                afterPosition,
                limit
            )
        ),
        QueryAppleVarietySugarAcidityZonePointsService::toPagePosition,
        variety -> toResultIfMatches(variety, query)
    );
  }

  private @NonNull AppleVarietySugarAcidityZoneResult toResult(
      @NonNull AppleVarietyAnalysisCandidateView variety,
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        evaluation,
        ChemicalProfileAxesPotentialAbvAdapter.predict(
            evaluation.axes(),
            potentialAbvPredictor
        ).orElse(null) // NOSONAR
    );
  }

  private @NonNull Optional<@NonNull AppleVarietySugarAcidityZoneResult> toResultIfMatches(
      @NonNull AppleVarietyAnalysisCandidateView variety,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    final var evaluation = chemicalProfileAssessmentAdapter.project(variety.chemicalProfile());
    if (!chemicalProfileAssessmentMatcher.matches(
        evaluation,
        query.filters().capability(),
        query.filters().sugarAcidityZone()
    )) {
      return Optional.empty();
    }

    return Optional.of(toResult(variety, evaluation));
  }

  private static @NonNull AppleVarietyCatalogPagePosition toPagePosition(
      @NonNull AppleVarietyAnalysisCandidateView variety
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    return new AppleVarietyCatalogPagePosition(
        variety.name().value(),
        variety.varietyId().value()
    );
  }
}
