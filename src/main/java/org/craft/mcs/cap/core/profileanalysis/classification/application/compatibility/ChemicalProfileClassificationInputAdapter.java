package org.craft.mcs.cap.core.profileanalysis.classification.application.compatibility;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.sugar.SugarCharacteristicValues;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Transitional adapter from shared ChemicalProfile to classification-owned typed input. */
public final class ChemicalProfileClassificationInputAdapter {

  private ChemicalProfileClassificationInputAdapter() {
  }

  public static @NonNull CiderAppleClassificationInput adapt(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var sugar = profile.sugar().orElse(null); // NOSONAR
    return new CiderAppleClassificationInput(
        sugar == null ? null : SugarCharacteristicValues.asSpecificGravity(sugar),
        profile.titratableAcidity().orElse(null), // NOSONAR
        profile.tannin().orElse(null) // NOSONAR
    );
  }
}
