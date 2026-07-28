package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedCompositionCharacteristics;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceResponse;
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
public final class ChemicalProfileCompositionCommonResponseMapper {

  private ChemicalProfileCompositionCommonResponseMapper() {
  }

  public static @NonNull ChemicalProfileCompositionSourceResponse toResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "chemical profile composition source cannot be null");
    final var requestedVolume = requestedVolume(source);

    return new ChemicalProfileCompositionSourceResponse(
        toResponse(source.reference()),
        toRequestedVolumeResponse(requestedVolume),
        proportion(source)
    );
  }

  public static @Nullable ProfileTargetDistanceResponse toTargetDistanceResponse(
      @Nullable ProfileTargetDistance distance
  ) {
    return distance == null ? null : toResponse(distance);
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull ProfileTargetDistance distance
  ) {
    return ProfileTargetRestMapper.toResponse(distance);
  }

  public static @Nullable DecimalQuantityResponse toVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  public static @NonNull CalculatedCompositionCharacteristicsResponse toCharacteristicsResponse(
      @NonNull CalculatedCompositionCharacteristics characteristics
  ) {
    requireNonNull(characteristics, "calculated composition characteristics cannot be null");
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

  private static @Nullable BigDecimal requestedVolume(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable DecimalQuantityResponse toRequestedVolumeResponse(
      @Nullable BigDecimal requestedVolume
  ) {
    return requestedVolume == null
        ? null
        : new DecimalQuantityResponse(requestedVolume, CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable BigDecimal proportion(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal proportion) ? proportion : null;
  }

  private static @NonNull ChemicalProfileCompositionSourceReferenceRequest toResponse(
      @NonNull ChemicalProfileCompositionSourceReference reference
  ) {
    requireNonNull(reference, "chemical profile composition source reference cannot be null");
    return new ChemicalProfileCompositionSourceReferenceRequest(reference.type(), reference.id());
  }
}
