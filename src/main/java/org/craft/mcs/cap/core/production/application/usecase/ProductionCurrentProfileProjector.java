package org.craft.mcs.cap.core.production.application.usecase;

import org.craft.mcs.cap.core.production.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.application.model.ProductionCurrentProfileView;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.application.model.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Overlays current Production measurement evidence on the immutable initial profile snapshot. */
@Component
public final class ProductionCurrentProfileProjector {

  public @NonNull ProductionCurrentProfileView project(
      @NonNull ProductionProfileSnapshot initialProfile,
      @Nullable ProductionMeasuredProfileView measuredProfile
  ) {
    requireNonNull(initialProfile, "production initial profile cannot be null");
    if (measuredProfile == null) {
      return new ProductionCurrentProfileView(
          initial(initialProfile.specificGravity()),
          initial(initialProfile.titratableAcidity()),
          null,
          initialNullable(initialProfile.tannin()),
          initialNullable(initialProfile.yeastAssimilableNitrogen())
      );
    }
    return new ProductionCurrentProfileView(
        measuredOrInitial(
            measuredProfile.specificGravity(),
            initialProfile.specificGravity()
        ),
        measuredOrInitial(
            measuredProfile.titratableAcidity(),
            initialProfile.titratableAcidity()
        ),
        measuredProfile.ph(),
        measuredProfile.tannin() == null
            ? initialNullable(initialProfile.tannin())
            : measuredProfile.tannin(),
        measuredProfile.yeastAssimilableNitrogen() == null
            ? initialNullable(initialProfile.yeastAssimilableNitrogen())
            : measuredProfile.yeastAssimilableNitrogen()
    );
  }

  private static <T extends MeasuredValue> @NonNull ProductionCurrentCharacteristicView<T>
  measuredOrInitial(
      @Nullable ProductionCurrentCharacteristicView<T> measured,
      @NonNull T initialValue
  ) {
    return measured == null
        ? initial(initialValue)
        : requireNonNull(measured, "measured production characteristic cannot be null");
  }

  private static <T extends MeasuredValue> @NonNull ProductionCurrentCharacteristicView<T> initial(
      @NonNull T value
  ) {
    return ProductionCurrentCharacteristicView.fromInitialSnapshot(PointCharacteristicValue.of(value));
  }

  private static <T extends MeasuredValue> @Nullable ProductionCurrentCharacteristicView<T>
  initialNullable(
      @Nullable T value
  ) {
    return value == null ? null : initial(value);
  }
}
