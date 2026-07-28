package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.compatibility;

import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.sugar.SugarCharacteristicValues;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Transitional ACL from the broad chemical projection to composition source characteristics.
 *
 * <p>Remove it after context-owned read models expose typed SG, TA, tannin and YAN directly.</p>
 */
public final class ChemicalProfileCompositionSourceAdapter {

  private ChemicalProfileCompositionSourceAdapter() {
  }

  public static @NonNull Optional<@NonNull CompositionSourceCharacteristics> adapt(
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

    return Optional.of(new CompositionSourceCharacteristics(
        specificGravity,
        titratableAcidity,
        profile.tannin().orElse(null), // NOSONAR
        profile.yeastAssimilableNitrogen().orElse(null) // NOSONAR
    ));
  }
}
