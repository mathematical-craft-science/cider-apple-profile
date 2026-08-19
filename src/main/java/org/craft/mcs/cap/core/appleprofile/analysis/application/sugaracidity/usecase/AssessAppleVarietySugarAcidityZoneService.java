package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for interpreting one apple variety in sugar-acidity graph zones.
 *
 * <p>The service deliberately returns an unplottable result when sugar or acidity is missing. That
 * keeps incomplete catalog data visible to admin/catalog workflows instead of turning partial
 * profiles into server errors.
 */
@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyAnalysisProfileProvider profileProvider;
  private final @NonNull AppleVarietyGraphAnalysisProjector analysisProjector;

  @Override
  public @NonNull AppleVarietyGraphAnalysisView assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return analysisProjector.project(profileProvider.get(varietyId));
  }
}
