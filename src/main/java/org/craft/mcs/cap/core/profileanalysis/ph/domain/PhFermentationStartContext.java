package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Context for pH fermentation-start assessment.
 *
 * <p>The current chemical profile and planned/confirmed preparation actions are deliberately kept
 * separate. Preparation actions may explain a managed risk, but they do not modify pH by themselves.
 */
public record PhFermentationStartContext(
    @NonNull ChemicalProfile profile,
    @NonNull Set<@NonNull FermentationStartPreparationActionType> correctiveActions
) {

  public PhFermentationStartContext {
    requireNonNull(profile, "chemical profile cannot be null");
    correctiveActions = Set.copyOf(requireNonNull(correctiveActions, "pH corrective actions cannot be null"));
  }

  public static @NonNull PhFermentationStartContext withoutCorrectiveActions(@NonNull ChemicalProfile profile) {
    return new PhFermentationStartContext(profile, Set.of());
  }
}
