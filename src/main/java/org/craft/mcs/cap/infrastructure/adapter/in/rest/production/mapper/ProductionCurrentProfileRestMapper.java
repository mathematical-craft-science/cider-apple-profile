package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.math.BigDecimal;
import java.util.function.Function;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCurrentProfileView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.MeasurementEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionCurrentCharacteristicResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionCurrentProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps the typed current Production projection without recreating a universal profile model. */
public final class ProductionCurrentProfileRestMapper {

  private ProductionCurrentProfileRestMapper() {
  }

  public static @NonNull ProductionCurrentProfileResponse toResponse(
      @NonNull ProductionCurrentProfileView profile
  ) {
    requireNonNull(profile, "production current profile cannot be null");
    return new ProductionCurrentProfileResponse(
        toResponse(
            profile.specificGravity(),
            SpecificGravityValue::asBigDecimal,
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        toResponse(
            profile.titratableAcidity(),
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        toNullableResponse(profile.ph(), PhValue::asBigDecimal, CanonicalUnitLabels.PH),
        toNullableResponse(profile.tannin(), TanninValue::asBigDecimal, CanonicalUnitLabels.TANNIN),
        toNullableResponse(
            profile.yeastAssimilableNitrogen(),
            YeastAssimilableNitrogenValue::asBigDecimal,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        )
    );
  }

  private static <T extends MeasuredValue> @NonNull ProductionCurrentCharacteristicResponse toResponse(
      @NonNull ProductionCurrentCharacteristicView<T> characteristic,
      @NonNull Function<T, BigDecimal> decimalValue,
      @NonNull String unit
  ) {
    return new ProductionCurrentCharacteristicResponse(
        ProfileCharacteristicRestMapper.toResponse(characteristic.value(), decimalValue, unit),
        characteristic.origin(),
        characteristic.evidence() == null
            ? null
            : MeasurementEvidenceRestMapper.toResponse(characteristic.evidence()) // NOSONAR
    );
  }

  private static <T extends MeasuredValue> @Nullable ProductionCurrentCharacteristicResponse
  toNullableResponse(
      @Nullable ProductionCurrentCharacteristicView<T> characteristic,
      @NonNull Function<T, BigDecimal> decimalValue,
      @NonNull String unit
  ) {
    return characteristic == null ? null : toResponse(characteristic, decimalValue, unit);
  }
}
