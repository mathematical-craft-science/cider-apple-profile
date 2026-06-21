package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitsMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/**
 * Maps canonical CAP measurement unit metadata into REST transport DTOs.
 *
 * <p>The mapper keeps REST response assembly out of the controller and preserves one place where
 * core value-object ranges, display labels, and transport quantity wrappers are connected.
 */
public final class MeasurementUnitMetadataRestMapper {

  private MeasurementUnitMetadataRestMapper() {
  }

  public static @NonNull MeasurementUnitsMetadataResponse toResponse() {
    return new MeasurementUnitsMetadataResponse(
        List.of(
            unit(
                MeasurementUnitCode.SG,
                CanonicalUnitLabels.SUGAR_SHORT,
                CanonicalUnitLabels.SUGAR,
                SpecificGravityValue.minValue(),
                SpecificGravityValue.maxValue(),
                SpecificGravityValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.TA,
                CanonicalUnitLabels.TITRATABLE_ACIDITY_SHORT,
                CanonicalUnitLabels.TITRATABLE_ACIDITY,
                TitratableAcidityValue.minValue(),
                TitratableAcidityValue.maxValue(),
                TitratableAcidityValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.TANNINITY,
                CanonicalUnitLabels.TANNINITY_SHORT,
                CanonicalUnitLabels.TANNINITY,
                TanninityValue.minValue(),
                TanninityValue.maxValue(),
                TanninityValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.YAN,
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN_SHORT,
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN,
                YeastAssimilableNitrogenValue.minValue(),
                YeastAssimilableNitrogenValue.maxValue(),
                YeastAssimilableNitrogenValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.PH,
                CanonicalUnitLabels.PH_SHORT,
                CanonicalUnitLabels.PH,
                PhValue.minValue(),
                PhValue.maxValue(),
                PhValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.ABV,
                CanonicalUnitLabels.ABV_SHORT,
                CanonicalUnitLabels.ABV,
                AbvValue.minValue(),
                AbvValue.maxValue(),
                AbvValue.stepValue()
            ),
            unit(
                MeasurementUnitCode.VOLUME,
                CanonicalUnitLabels.VOLUME_SHORT,
                CanonicalUnitLabels.VOLUME,
                VolumeValue.minValue(),
                VolumeValue.maxValue(),
                VolumeValue.stepValue()
            )
        )
    );
  }

  private static @NonNull MeasurementUnitMetadataResponse unit(
      @NonNull MeasurementUnitCode code,
      @NonNull String shortLabel,
      @NonNull String fullLabel,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max,
      @NonNull BigDecimal step
  ) {
    return new MeasurementUnitMetadataResponse(
        code,
        fullLabel,
        shortLabel,
        fullLabel,
        quantity(min, fullLabel),
        quantity(max, fullLabel),
        quantity(step, fullLabel)
    );
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }
}
