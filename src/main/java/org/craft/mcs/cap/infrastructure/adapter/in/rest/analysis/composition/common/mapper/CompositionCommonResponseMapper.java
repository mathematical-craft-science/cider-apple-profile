package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceReferenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;


/** Maps calculated composition values to shared REST response fragments. */
public final class CompositionCommonResponseMapper {

  private CompositionCommonResponseMapper() {
  }

  public static @NonNull CompositionSourceResponse toResponse(
      @NonNull CompositionSourceInput source
  ) {
    requireNonNull(source, "composition source input cannot be null");
    final var requestedVolume = source.weight() instanceof CompositionSourceInput.AbsoluteVolume(var volume)
        ? volume.liters()
        : null;
    return new CompositionSourceResponse(
        new CompositionSourceReferenceResponse(source.reference().type(), source.reference().id()),
        toRequestedVolumeResponse(requestedVolume),
        source.weight() instanceof CompositionSourceInput.Proportion(var proportion) ? proportion : null
    );
  }

  public static @Nullable ProfileTargetDistanceResponse toTargetDistanceResponse(
      @Nullable FormulationTargetDistanceView distance
  ) {
    return distance == null ? null : ProfileTargetRestMapper.toResponse(distance);
  }

  public static @Nullable DecimalQuantityResponse toVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  public static @NonNull CalculatedCompositionCharacteristicsResponse toCharacteristicsResponse(
      CalculatedCompositionView.@NonNull Characteristics characteristics
  ) {
    return new CalculatedCompositionCharacteristicsResponse(
        new DecimalQuantityResponse(
            characteristics.specificGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        new DecimalQuantityResponse(
            characteristics.titratableAcidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        quantity(characteristics.tannin(), TanninValue::asBigDecimal, CanonicalUnitLabels.TANNIN),
        quantity(
            characteristics.yeastAssimilableNitrogen(),
            YeastAssimilableNitrogenValue::asBigDecimal,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        )
    );
  }

  public static @NonNull CalculatedCompositionCharacteristicsResponse toCharacteristicsResponse(
      FeedstockCompositionOptimizationView.@NonNull Characteristics characteristics
  ) {
    return new CalculatedCompositionCharacteristicsResponse(
        new DecimalQuantityResponse(
            characteristics.specificGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        new DecimalQuantityResponse(
            characteristics.titratableAcidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        quantity(characteristics.tannin(), TanninValue::asBigDecimal, CanonicalUnitLabels.TANNIN),
        quantity(
            characteristics.yeastAssimilableNitrogen(),
            YeastAssimilableNitrogenValue::asBigDecimal,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        )
    );
  }

  private static <T> @Nullable DecimalQuantityResponse quantity(
      @Nullable T value,
      @NonNull Function<T, BigDecimal> decimalValue,
      @NonNull String unit
  ) {
    return value == null ? null : new DecimalQuantityResponse(decimalValue.apply(value), unit);
  }

  private static @Nullable DecimalQuantityResponse toRequestedVolumeResponse(
      @Nullable BigDecimal requestedVolume
  ) {
    return requestedVolume == null
        ? null
        : new DecimalQuantityResponse(requestedVolume, CanonicalUnitLabels.VOLUME);
  }

}
