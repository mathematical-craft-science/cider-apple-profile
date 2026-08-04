package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.service.AppleVarietyGraphCandidateMatcher;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateCriteria;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
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
  private final @NonNull AppleVarietyGraphCandidateMatcher graphCandidateMatcher;
  private final @NonNull AppleVarietyGraphAnalysisProjector analysisProjector;

  @Override
  public @NonNull CursorPage<
      @NonNull AppleVarietyGraphAnalysisView,
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

  private @NonNull Optional<@NonNull AppleVarietyGraphAnalysisView> toResultIfMatches(
      @NonNull AppleVarietyAnalysisCandidateView variety,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    final var referenceProfile = variety.referenceProfile();
    final var analysis = analysisProjector.project(
        variety.varietyId(),
        variety.name(),
        referenceProfile
    );
    if (!graphCandidateMatcher.matches(referenceProfile, analysis.graphEvaluation(), query.filters())) {
      return Optional.empty();
    }

    return Optional.of(analysis);
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
