package org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.AppleVarietyAnalysisProfileNotFoundException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.catalog.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.GetAppleVarietyReferenceProfileUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL adapter exposing one catalog reference profile through the analysis-owned port. */
@Component
@RequiredArgsConstructor
final class CatalogAppleVarietyAnalysisProfileAdapter
    implements AppleVarietyAnalysisProfileProvider {

  private final @NonNull GetAppleVarietyReferenceProfileUseCase catalogQuery;

  @Override
  public @NonNull AppleVarietyAnalysisProfile get(@NonNull AppleVarietyId varietyId) {
    try {
      return AppleVarietyAnalysisProfileMapper.toAnalysisProfile(catalogQuery.get(varietyId));
    } catch (AppleVarietyNotFoundException exception) {
      throw new AppleVarietyAnalysisProfileNotFoundException(exception.varietyId());
    }
  }
}
