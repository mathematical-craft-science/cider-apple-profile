package org.craft.mcs.cap.core.profileanalysis.yan.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Context for YAN fermentation-start assessment.
 *
 * <p>The chemical profile and preparation actions are separate: nutrient plans can explain managed
 * risk, but they do not change measured YAN without a confirmed operation and new measurement.
 */
public record YanFermentationStartContext(
    @NonNull ChemicalProfile profile,
    @NonNull Set<@NonNull FermentationStartPreparationActionType> preparationActions
) {

  public YanFermentationStartContext {
    requireNonNull(profile, "chemical profile cannot be null");
    preparationActions = Set.copyOf(
        requireNonNull(preparationActions, "YAN preparation actions cannot be null")
    );
  }

  public static @NonNull YanFermentationStartContext withoutPreparationActions(
      @NonNull ChemicalProfile profile
  ) {
    return new YanFermentationStartContext(profile, Set.of());
  }
}
