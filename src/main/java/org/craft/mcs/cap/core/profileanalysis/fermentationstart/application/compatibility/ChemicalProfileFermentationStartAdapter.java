package org.craft.mcs.cap.core.profileanalysis.fermentationstart.application.compatibility;

import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.sugar.SugarCharacteristicValues;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Adapts the transitional ChemicalProfile to fermentation-start-owned typed evidence. */
public final class ChemicalProfileFermentationStartAdapter {

  private ChemicalProfileFermentationStartAdapter() {
  }

  public static @NonNull FermentationStartProfile adapt(
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var sugar = profile.sugar().orElse(null); // NOSONAR
    return new FermentationStartProfile(
        sugar == null ? null : SugarCharacteristicValues.asSpecificGravity(sugar),
        profile.ph().orElse(null), // NOSONAR
        profile.yeastAssimilableNitrogen().orElse(null) // NOSONAR
    );
  }
}
