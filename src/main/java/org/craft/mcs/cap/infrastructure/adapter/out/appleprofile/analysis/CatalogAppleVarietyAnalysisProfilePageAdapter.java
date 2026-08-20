package org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfilePageProvider;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.QueryAppleVarietyReferenceProfilePageUseCase;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL adapter exposing ordered catalog profiles through the analysis-owned page port. */
@Component
@RequiredArgsConstructor
final class CatalogAppleVarietyAnalysisProfilePageAdapter
    implements AppleVarietyAnalysisProfilePageProvider {

  private final @NonNull QueryAppleVarietyReferenceProfilePageUseCase catalogQuery;

  @Override
  public @NonNull List<@NonNull AppleVarietyAnalysisProfile> findProfiles(
      @NonNull AppleVarietyAnalysisPageQuery query
  ) {
    requireNonNull(query, "apple variety analysis page query cannot be null");
    return catalogQuery.query(
        new AppleVarietyReferenceProfilePageQuery(
            query.nameContains(),
            toCatalogPosition(query.after()),
            query.limit()
        )
    ).stream()
        .map(AppleVarietyAnalysisProfileMapper::toAnalysisProfile)
        .toList();
  }

  private static @Nullable AppleVarietyCatalogPagePosition toCatalogPosition(
      @Nullable AppleVarietyAnalysisPagePosition position
  ) {
    return switch (position) {
      case null -> null;
      case AppleVarietyAnalysisPagePosition(var name, var varietyId) ->
          new AppleVarietyCatalogPagePosition(name, varietyId);
    };
  }
}
