package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceWeight;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceWeightRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionAxesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.ChemicalProfileCompositionOptimizationSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class ChemicalProfileCompositionCommonRestMapper {

  private ChemicalProfileCompositionCommonRestMapper() {
  }

  public static @NonNull ChemicalProfileCompositionSource toSource(
      @NonNull ChemicalProfileCompositionSourceRequest request
  ) {
    requireNonNull(request, "chemical profile composition calculation source request cannot be null");

    return new ChemicalProfileCompositionSource(
        toReference(request.reference()),
        toAxes(request.axes()),
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
        toAxes(request.axes()),
        toVolume(request.availableVolumeLiters())
    );
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

  public static @NonNull ProfileTargetObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return ProfileTargetObjectiveWeights.balanced();
    }

    return new ProfileTargetObjectiveWeights(
        weightOrDefault(request.sugar()),
        weightOrDefault(request.acidity()),
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

  public static @Nullable ProfileTargetDistanceResponse toTargetDistanceResponse(
      @Nullable ProfileTargetDistance distance
  ) {
    return distance == null ? null : toResponse(distance);
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(@NonNull ProfileTargetDistance distance) {
    requireNonNull(distance, "profile target distance cannot be null");

    return new ProfileTargetDistanceResponse(
        distance.status(),
        distance.value(),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        toResponse(distance.nearestTargetPoint()),
        quantity(distance.tannin(), CanonicalUnitLabels.TANNINITY),
        quantity(distance.nearestTargetTannin(), CanonicalUnitLabels.TANNINITY),
        distance.objectiveFunction(),
        distance.metric(),
        distance.targetKind(),
        distance.targetShape(),
        distance.missingMeasurements()
    );
  }

  public static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  public static @Nullable DecimalQuantityResponse toVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  public static @Nullable SugarAcidityAxisPointResponse toYeastAssimilableNitrogenResponse(
      @Nullable SugarAcidityAxisPoint point
  ) {
    return point == null
        ? null
        : SugarAcidityZoneRestMapper.toResponse(
            point,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        );
  }

  private static @Nullable BigDecimal requestedVolume(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable DecimalQuantityResponse toRequestedVolumeResponse(@Nullable BigDecimal requestedVolume) {
    return requestedVolume == null
        ? null
        : new DecimalQuantityResponse(requestedVolume, CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable BigDecimal proportion(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal proportion) ? proportion : null;
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

  private static @NonNull ChemicalProfileCompositionSourceReferenceRequest toResponse(
      @NonNull ChemicalProfileCompositionSourceReference reference
  ) {
    requireNonNull(reference, "chemical profile composition source reference cannot be null");
    return new ChemicalProfileCompositionSourceReferenceRequest(reference.type(), reference.id());
  }

  private static @NonNull ChemicalProfileAxes toAxes(@NonNull ChemicalProfileCompositionAxesRequest request) {
    requireNonNull(request, "chemical profile composition source profile axes request cannot be null");

    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, requireNonNull(request.sugar(), "composition source sugar cannot be null")),
        axis(ChemicalAxis.ACIDITY, requireNonNull(request.acidity(), "composition source acidity cannot be null")),
        axisOrNull(ChemicalAxis.TANNINITY, request.tannin()),
        axisOrNull(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, request.yeastAssimilableNitrogen())
    );
  }

  private static @NonNull ChemicalProfileCompositionSourceWeight toWeight(
      @NonNull ChemicalProfileCompositionSourceWeightRequest request
  ) {
    requireNonNull(request, "chemical profile composition source weight request cannot be null");
    final var kind = requireNonNull(request.kind(), "chemical profile composition source weight kind cannot be null");
    final var value = requireNonNull(request.value(), "chemical profile composition source weight value cannot be null");

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
      case PredefinedZoneProfileTargetRequest predefined -> targetResolver.resolvePredefinedZone(predefined.zone());
      case CustomRectangleProfileTargetRequest custom -> new CustomRectangleSugarAcidityTarget(
          new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          )
      );
    };
  }

  private static @NonNull TanninTargetRange toTanninTarget(@NonNull ProfileTargetTanninRangeRequest request) {
    requireNonNull(request, "profile target tannin range request cannot be null");
    return new TanninTargetRange(request.min(), request.max());
  }

  private static @NonNull SugarAcidityCoordinatesResponse toResponse(
      @NonNull SugarAcidityCoordinates coordinates
  ) {
    requireNonNull(coordinates, "sugar-acidity coordinates cannot be null");
    return new SugarAcidityCoordinatesResponse(
        new DecimalQuantityResponse(coordinates.sugar(), CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(coordinates.acidity(), CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @Nullable DecimalQuantityResponse quantity(
      @Nullable BigDecimal value,
      @NonNull String unit
  ) {
    return value == null ? null : new DecimalQuantityResponse(value, unit);
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }

  private static @NonNull AxisValue axis(
      @NonNull ChemicalAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(value, "chemical axis value cannot be null");

    return new AxisValue(axis, value, MeasurementValueKind.EXACT, null, null, null, null);
  }

  private static @Nullable AxisValue axisOrNull(
      @NonNull ChemicalAxis axis,
      @Nullable BigDecimal value
  ) {
    return value == null ? null : axis(axis, value);
  }
}
