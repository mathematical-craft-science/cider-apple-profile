package org.craft.mcs.cap.core.cideranalysis.readiness.domain.yan;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Context for YAN fermentation-start assessment.
 *
 * <p>The YAN evidence and preparation actions are separate: nutrient plans can explain managed
 * risk, but they do not change measured YAN without a confirmed operation and new measurement.
 */
public record YanFermentationStartContext(
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen,
    @NonNull Set<@NonNull FermentationStartPreparationActionType> preparationActions
) {

  public YanFermentationStartContext {
    preparationActions = Set.copyOf(
        requireNonNull(preparationActions, "YAN preparation actions cannot be null")
    );
  }

  public static @NonNull YanFermentationStartContext withoutPreparationActions(
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {
    return new YanFermentationStartContext(yeastAssimilableNitrogen, Set.of());
  }
}
