package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Apple-variety identity bound to a sugar-acidity evaluation backed by SG and TA evidence.
 */
public record AppleVarietySugarAcidityZoneResult(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
    @Nullable PotentialAbvPrediction potentialAbv
) {

  public AppleVarietySugarAcidityZoneResult {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
  }
}
