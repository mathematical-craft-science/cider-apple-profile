package org.craft.mcs.cap.production.testing;

import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Typed Production source fixtures shared by candidate, confirmation and traceability tests. */
public final class ProductionSourceCharacteristicsFixtures {

  private ProductionSourceCharacteristicsFixtures() {
  }

  public static @NonNull ProductionSourceCharacteristics productionSourceCharacteristics(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin,
      @Nullable String yeastAssimilableNitrogen
  ) {
    return new ProductionSourceCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin)),
        yeastAssimilableNitrogen == null
            ? null
            : PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of(yeastAssimilableNitrogen))
    );
  }
}
