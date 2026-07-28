package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.compatibility;

import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.sugar.SugarCharacteristicValues;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Transitional adapter from the shared chemical projection to target-scoring characteristics.
 *
 * <p>Remove it after all context-owned profiles expose typed SG, TA and tannin directly.</p>
 */
public final class ChemicalProfileTargetScoringInputAdapter {

  private ChemicalProfileTargetScoringInputAdapter() {
  }

  public static @NonNull Optional<@NonNull ProfileTargetScoringInput> adapt(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var specificGravity = profile.sugar()
        .map(SugarCharacteristicValues::asSpecificGravity)
        .orElse(null); // NOSONAR
    final var titratableAcidity = profile.titratableAcidity().orElse(null); // NOSONAR
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }
    return Optional.of(new ProfileTargetScoringInput(
        specificGravity,
        titratableAcidity,
        profile.tannin().orElse(null) // NOSONAR
    ));
  }
}
