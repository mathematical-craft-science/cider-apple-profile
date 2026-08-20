package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfilePageProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.QueryAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side use case for catalog variety points on the sugar-acidity graph.
 *
 * <p>The service performs cursor pagination over catalog profile inputs, applies application-level
 * graph filters, and returns both plottable and unplottable results according to the
 * requested filters. Catalog access remains behind the analysis-owned profile page port.
 */
@Component
@RequiredArgsConstructor
public final class QueryAppleVarietySugarAcidityZonePointsService
    implements QueryAppleVarietySugarAcidityZonePointsUseCase {

  private final @NonNull AppleVarietyAnalysisProfilePageProvider profilePageProvider;
  private final @NonNull AppleVarietyGraphAnalysisProjector analysisProjector;

  @Override
  public @NonNull CursorPage<
      @NonNull AppleVarietyGraphAnalysisView,
      @NonNull AppleVarietyAnalysisPagePosition
  > query(
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> profilePageProvider.findProfiles(
            new AppleVarietyAnalysisPageQuery(
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
      @NonNull AppleVarietyAnalysisProfile variety,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    final var analysis = analysisProjector.project(variety);
    final var graphCriteria = query.graphCriteria();
    if (graphCriteria != null && !graphCriteria.matches(analysis.graphEvaluation())) {
      return Optional.empty();
    }

    return Optional.of(analysis);
  }

  private static @NonNull AppleVarietyAnalysisPagePosition toPagePosition(
      @NonNull AppleVarietyAnalysisProfile variety
  ) {
    requireNonNull(variety, "apple variety cannot be null");
    return new AppleVarietyAnalysisPagePosition(
        variety.name().value(),
        variety.varietyId().value()
    );
  }
}
