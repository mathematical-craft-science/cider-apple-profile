package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class ProfileTargetRestMapper {

  private ProfileTargetRestMapper() {
  }

  public static @Nullable ProfileTargetObjective toTargetObjective(
      @Nullable ProfileTargetRequest request,
      @Nullable ProfileTargetTanninRangeRequest tanninTarget,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    if (request == null) {
      return null;
    }

    final var sugarAcidityTarget = toTarget(request, targetResolver);
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(sugarAcidityTarget);
    }

    return ProfileTargetObjective.withTanninTarget(sugarAcidityTarget, toTanninTarget(tanninTarget));
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

  public static @NonNull ProfileAxisNormalizerCode normalizerFor(
      @NonNull ProfileTargetScoringPurpose scoringPurpose
  ) {
    requireNonNull(scoringPurpose, "profile target scoring purpose cannot be null");
    return switch (scoringPurpose) {
      case GRAPH_EXPLORATION -> ProfileAxisNormalizerCode.MIN_MAX;
      case BLENDING_PLANNING -> ProfileAxisNormalizerCode.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> ProfileAxisNormalizerCode.THRESHOLD_SEGMENT;
      case EXPERT_CALIBRATION -> ProfileAxisNormalizerCode.SENSITIVITY_UNIT;
    };
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull ProfileTargetDistance distance
  ) {
    requireNonNull(distance, "profile target distance cannot be null");
    final var fit = distance.fitAssessment();

    return new ProfileTargetDistanceResponse(
        distance.status(),
        distance.value(),
        fit.matchPercent(),
        fit.status(),
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

  private static @NonNull SugarAcidityTarget toTarget(
      @NonNull ProfileTargetRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "profile target request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

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

  private static @NonNull TanninTargetRange toTanninTarget(
      @NonNull ProfileTargetTanninRangeRequest request
  ) {
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
}
