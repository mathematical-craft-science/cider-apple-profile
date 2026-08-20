package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CompositionSourceWeightRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceCharacteristicsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.CompositionOptimizationSourceRequest;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;


/**
 * Maps common composition request fragments to typed calculation and target values.
 */
public final class CompositionCommonRequestMapper {

  private CompositionCommonRequestMapper() {
  }

  public static @NonNull CompositionSourceInput toSourceInput(
      @NonNull CompositionSourceRequest request
  ) {
    requireNonNull(request, "composition calculation source request cannot be null");
    final var characteristics = toCharacteristics(request.characteristics());
    return new CompositionSourceInput(
        new CompositionSourceInput.Reference(request.reference().type(), request.reference().id()),
        characteristics,
        toWeight(request.weight()),
        toVolume(request.availableVolumeLiters())
    );
  }

  public static @NonNull OptimizationSourceInput toOptimizationSource(
      @NonNull CompositionOptimizationSourceRequest request
  ) {
    requireNonNull(request, "composition optimization source request cannot be null");

    final var characteristics = toCharacteristics(request.characteristics());
    return new OptimizationSourceInput(
        new CompositionSourceInput.Reference(request.reference().type(), request.reference().id()),
        characteristics,
        toVolume(request.availableVolumeLiters())
    );
  }

  public static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static CompositionSourceInput.@NonNull Characteristics toCharacteristics(
      @NonNull CompositionSourceCharacteristicsRequest request
  ) {
    requireNonNull(request, "composition source characteristics request cannot be null");

    return new CompositionSourceInput.Characteristics(
        PointCharacteristicValue.of(
            SpecificGravityValue.of(
                requireNonNull(
                    request.specificGravity(),
                    "composition source specific gravity cannot be null"
                )
            )
        ),
        PointCharacteristicValue.of(
            TitratableAcidityValue.of(
                requireNonNull(
                    request.titratableAcidity(),
                    "composition source titratable acidity cannot be null"
                )
            )
        ),
        request.tannin() == null
        ? null
        : PointCharacteristicValue.of(TanninValue.of(request.tannin())),  // NOSONAR
        request.yeastAssimilableNitrogen() == null
        ? null
        : PointCharacteristicValue.of(
            YeastAssimilableNitrogenValue.of(request.yeastAssimilableNitrogen()) // NOSONAR
        )
    );
  }

  private static CompositionSourceInput.@NonNull Weight toWeight(
      @NonNull CompositionSourceWeightRequest request
  ) {
    requireNonNull(request, "composition source weight request cannot be null");
    final var kind = requireNonNull(request.kind(), "composition source weight kind cannot be null");
    final var value = requireNonNull(
        request.value(),
        "composition source weight value cannot be null"
    );

    return switch (kind) {
      case ABSOLUTE_VOLUME_LITERS -> new CompositionSourceInput.AbsoluteVolume(VolumeValue.ofLiters(value));
      case PROPORTION -> new CompositionSourceInput.Proportion(value);
    };
  }

}
