package org.craft.mcs.cap.core.production.application.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.Nullable;


/**
 * Current Production characteristics backed specifically by measurement evidence.
 *
 * <p>This projection does not include values inherited from the accepted initial production
 * snapshot. {@link ProductionCurrentProfileView} composes both sources into the effective current
 * profile.</p>
 */
public record ProductionMeasuredProfileView(
    @Nullable ProductionCurrentCharacteristicView<SpecificGravityValue> specificGravity,
    @Nullable ProductionCurrentCharacteristicView<TitratableAcidityValue> titratableAcidity,
    @Nullable ProductionCurrentCharacteristicView<PhValue> ph,
    @Nullable ProductionCurrentCharacteristicView<TanninValue> tannin,
    @Nullable ProductionCurrentCharacteristicView<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public ProductionMeasuredProfileView {
    requireMeasured(specificGravity);
    requireMeasured(titratableAcidity);
    requireMeasured(ph);
    requireMeasured(tannin);
    requireMeasured(yeastAssimilableNitrogen);
  }

  private static <T extends MeasuredValue> void requireMeasured(
      @Nullable ProductionCurrentCharacteristicView<T> characteristic
  ) {
    if (characteristic != null && characteristic.origin() != ProductionCharacteristicOrigin.MEASUREMENT) {
      throw new ApplicationValidationException(
          "production measured profile accepts only measurement-backed characteristics"
      );
    }
  }
}
