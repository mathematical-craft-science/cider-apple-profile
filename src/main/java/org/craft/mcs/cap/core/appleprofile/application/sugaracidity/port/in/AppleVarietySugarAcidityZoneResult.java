package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Apple-variety identity bound to a generic sugar-acidity profile evaluation.
 */
public record AppleVarietySugarAcidityZoneResult(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull SugarAcidityProfileEvaluation evaluation
) {

  public AppleVarietySugarAcidityZoneResult {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
  }
}
