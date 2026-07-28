package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceWeight;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceWeightRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceCharacteristicsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.ChemicalProfileCompositionOptimizationSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
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
import static java.util.Objects.requireNonNullElse;


/**
 * Maps common composition request fragments to typed calculation and target values.
 */
public final class ChemicalProfileCompositionCommonRequestMapper {

  private ChemicalProfileCompositionCommonRequestMapper() {
  }

  public static @NonNull ChemicalProfileCompositionSource toSource(
      @NonNull ChemicalProfileCompositionSourceRequest request
  ) {
    requireNonNull(request, "chemical profile composition calculation source request cannot be null");

    return new ChemicalProfileCompositionSource(
        toReference(request.reference()),
        toCharacteristics(request.characteristics()),
        toWeight(request.weight()),
        toVolume(request.availableVolumeLiters())
    );
  }

  public static @NonNull ChemicalProfileCompositionOptimizationSource toOptimizationSource(
      @NonNull ChemicalProfileCompositionOptimizationSourceRequest request
  ) {
    requireNonNull(request, "chemical profile composition optimization source request cannot be null");

    return new ChemicalProfileCompositionOptimizationSource(
        toReference(request.reference()),
        toCharacteristics(request.characteristics()),
        toVolume(request.availableVolumeLiters())
    );
  }

  public static @NonNull ProfileTargetObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return ProfileTargetObjectiveWeights.balanced();
    }

    return new ProfileTargetObjectiveWeights(
        weightOrDefault(request.specificGravity()),
        weightOrDefault(request.titratableAcidity()),
        request.tannin()
    );
  }

  public static @Nullable ProfileTargetObjective toTargetObjective(
      @Nullable ProfileTargetRequest request,
      @Nullable ProfileTargetTanninRangeRequest tanninTarget,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    if (request == null) {
      return null;
    }

    final var sugarAcidityTarget = toTarget(request, targetResolver);
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(sugarAcidityTarget);
    }

    return ProfileTargetObjective.withTanninTarget(sugarAcidityTarget, toTanninTarget(tanninTarget));
  }

  public static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static @NonNull ChemicalProfileCompositionSourceReference toReference(
      @NonNull ChemicalProfileCompositionSourceReferenceRequest request
  ) {
    requireNonNull(request, "chemical profile composition source reference request cannot be null");
    return new ChemicalProfileCompositionSourceReference(
        requireNonNull(request.type(), "chemical profile composition source reference type cannot be null"),
        requireNonNull(request.id(), "chemical profile composition source reference id cannot be null")
    );
  }

  private static @NonNull CompositionSourceCharacteristics toCharacteristics(
      @NonNull CompositionSourceCharacteristicsRequest request
  ) {
    requireNonNull(request, "composition source characteristics request cannot be null");

    return new CompositionSourceCharacteristics(
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

  private static @NonNull ChemicalProfileCompositionSourceWeight toWeight(
      @NonNull ChemicalProfileCompositionSourceWeightRequest request
  ) {
    requireNonNull(request, "chemical profile composition source weight request cannot be null");
    final var kind = requireNonNull(request.kind(), "chemical profile composition source weight kind cannot be null");
    final var value = requireNonNull(
        request.value(),
        "chemical profile composition source weight value cannot be null"
    );

    return switch (kind) {
      case ABSOLUTE_VOLUME_LITERS -> new AbsoluteCompositionVolume(VolumeValue.ofLiters(value));
      case PROPORTION -> new CompositionProportion(value);
    };
  }

  private static @NonNull SugarAcidityTarget toTarget(
      @NonNull ProfileTargetRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest(_, var zone) -> targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleProfileTargetRequest customRectangle -> new CustomRectangleSugarAcidityTarget(
          ProfileTargetRestMapper.toRectangleGeometry(customRectangle)
      );
    };
  }

  private static @NonNull TanninTargetRange toTanninTarget(
      @NonNull ProfileTargetTanninRangeRequest request
  ) {
    requireNonNull(request, "profile target tannin range request cannot be null");
    return ProfileTargetRestMapper.toTanninTarget(request);
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }
}
