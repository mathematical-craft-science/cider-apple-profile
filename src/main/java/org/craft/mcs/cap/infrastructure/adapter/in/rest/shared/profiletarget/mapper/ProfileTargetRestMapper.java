package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
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
        weightOrDefault(request.specificGravity()),
        weightOrDefault(request.titratableAcidity()),
        request.tannin()
    );
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
        tanninQuantity(distance.tannin()),
        tanninQuantity(distance.nearestTargetTannin()),
        distance.objectiveFunction(),
        distance.metric(),
        distance.targetKind(),
        distance.targetShape(),
        distance.missingAxes().stream()
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  private static @NonNull SugarAcidityTarget toTarget(
      @NonNull ProfileTargetRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "profile target request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    return switch (request) {
      case PredefinedZoneProfileTargetRequest(_, var zone) -> targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleProfileTargetRequest customRectangle ->
          new CustomRectangleSugarAcidityTarget(
              toRectangleGeometry(customRectangle)
          );
    };
  }

  public static @NonNull RectangleSugarAcidityTargetGeometry toRectangleGeometry(
      @NonNull CustomRectangleProfileTargetRequest request
  ) {
    requireNonNull(request, "custom rectangle profile target request cannot be null");
    return new RectangleSugarAcidityTargetGeometry(
        MeasuredValueRange.closed(
            SpecificGravityValue.of(request.specificGravityMin()),
            SpecificGravityValue.of(request.specificGravityMax())
        ),
        MeasuredValueRange.closed(
            TitratableAcidityValue.of(request.titratableAcidityMin()),
            TitratableAcidityValue.of(request.titratableAcidityMax())
        )
    );
  }

  public static @NonNull TanninTargetRange toTanninTarget(
      @NonNull ProfileTargetTanninRangeRequest request
  ) {
    requireNonNull(request, "profile target tannin range request cannot be null");
    return new TanninTargetRange(
        TanninValue.of(request.min()),
        TanninValue.of(request.max())
    );
  }

  private static @NonNull ProfileTargetCoordinatesResponse toResponse(
      @NonNull SugarAcidityGraphPoint point
  ) {
    requireNonNull(point, "SG/TA graph point cannot be null");
    return new ProfileTargetCoordinatesResponse(
        new DecimalQuantityResponse(
            point.specificGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        new DecimalQuantityResponse(
            point.titratableAcidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        )
    );
  }

  private static @Nullable DecimalQuantityResponse tanninQuantity(
      @Nullable TanninValue value
  ) {
    return value == null
        ? null
        : new DecimalQuantityResponse(value.asBigDecimal(), CanonicalUnitLabels.TANNIN);
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }
}
