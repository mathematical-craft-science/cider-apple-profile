package org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Context for pH fermentation-start assessment.
 *
 * <p>The current pH evidence and planned/confirmed preparation actions are deliberately kept
 * separate. Preparation actions may explain a managed risk, but they do not modify pH by themselves.
 */
public record PhFermentationStartContext(
    @Nullable CharacteristicValue<PhValue> ph,
    @NonNull Set<@NonNull FermentationStartPreparationActionType> correctiveActions
) {

  public PhFermentationStartContext {
    correctiveActions = Set.copyOf(requireNonNull(correctiveActions, "pH corrective actions cannot be null"));
  }

  public static @NonNull PhFermentationStartContext withoutCorrectiveActions(
      @Nullable CharacteristicValue<PhValue> ph
  ) {
    return new PhFermentationStartContext(ph, Set.of());
  }
}
