package org.craft.mcs.cap.core.appleprofile.application.abv.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyPotentialAbvPrediction(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull MeasurementValueKind originalGravityValueKind,
    @NonNull PotentialAbvPrediction prediction
) {

  public AppleVarietyPotentialAbvPrediction {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(originalGravityValueKind, "original gravity value kind cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
