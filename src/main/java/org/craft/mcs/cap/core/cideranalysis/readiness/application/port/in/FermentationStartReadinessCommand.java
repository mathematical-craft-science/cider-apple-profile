package org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartPreparationAction;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable characteristic snapshot used for fermentation-start readiness analysis. */
public record FermentationStartReadinessCommand(
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen,
    @NonNull Set<@NonNull FermentationStartPreparationAction> preparationActions
) {

  public FermentationStartReadinessCommand {
    preparationActions = Set.copyOf(requireNonNull(preparationActions, "preparation actions cannot be null"));
  }

  public static @NonNull FermentationStartReadinessCommand withoutPreparationActions(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<PhValue> ph,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {
    return new FermentationStartReadinessCommand(specificGravity, ph, yeastAssimilableNitrogen, Set.of());
  }
}
