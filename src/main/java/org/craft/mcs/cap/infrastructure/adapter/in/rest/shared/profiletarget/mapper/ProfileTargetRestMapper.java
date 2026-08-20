package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import java.util.Comparator;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileDistanceMetric;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.SugarAcidityTargetKind;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ProfileTargetRestMapper {

  private ProfileTargetRestMapper() {
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull FeedstockTargetDistanceView distance
  ) {
    requireNonNull(distance, "feedstock target distance cannot be null");
    return new ProfileTargetDistanceResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceStatus
            .valueOf(distance.status().name()),
        distance.value(),
        distance.matchPercent(),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetFitStatus
            .valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        new ProfileTargetCoordinatesResponse(
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().specificGravity().asBigDecimal(),
                CanonicalUnitLabels.SPECIFIC_GRAVITY
            ),
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().titratableAcidity().asBigDecimal(),
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            )
        ),
        tanninQuantity(distance.tannin()),
        tanninQuantity(distance.nearestTargetTannin()),
        ProfileTargetObjectiveFunctionCode.valueOf(distance.objectiveFunction().name()),
        ProfileDistanceMetric.valueOf(distance.metric().name()),
        SugarAcidityTargetKind.valueOf(distance.targetKind().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape
            .valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull AppleTargetDistanceView distance
  ) {
    requireNonNull(distance, "apple target distance cannot be null");
    return new ProfileTargetDistanceResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceStatus
            .valueOf(distance.status().name()),
        distance.value(),
        distance.matchPercent(),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetFitStatus
            .valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        new ProfileTargetCoordinatesResponse(
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().specificGravity().asBigDecimal(),
                CanonicalUnitLabels.SPECIFIC_GRAVITY
            ),
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().titratableAcidity().asBigDecimal(),
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            )
        ),
        tanninQuantity(distance.tannin()),
        tanninQuantity(distance.nearestTargetTannin()),
        ProfileTargetObjectiveFunctionCode.valueOf(distance.objectiveFunction().name()),
        ProfileDistanceMetric.valueOf(distance.metric().name()),
        SugarAcidityTargetKind.valueOf(distance.targetKind().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape
            .valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull FormulationTargetDistanceView distance
  ) {
    requireNonNull(distance, "formulation target distance cannot be null");
    return new ProfileTargetDistanceResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceStatus
            .valueOf(distance.status().name()),
        distance.value(),
        distance.matchPercent(),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetFitStatus
            .valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        new ProfileTargetCoordinatesResponse(
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().specificGravity().asBigDecimal(),
                CanonicalUnitLabels.SPECIFIC_GRAVITY
            ),
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().titratableAcidity().asBigDecimal(),
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            )
        ),
        tanninQuantity(distance.tannin()),
        tanninQuantity(distance.nearestTargetTannin()),
        ProfileTargetObjectiveFunctionCode.valueOf(distance.objectiveFunction().name()),
        ProfileDistanceMetric.valueOf(distance.metric().name()),
        SugarAcidityTargetKind.valueOf(distance.targetKind().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape
            .valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  public static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull ProductionCandidateTargetDistanceView distance
  ) {
    requireNonNull(distance, "production candidate target distance cannot be null");
    return new ProfileTargetDistanceResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceStatus
            .valueOf(distance.status().name()),
        distance.value(),
        distance.matchPercent(),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetFitStatus
            .valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        new ProfileTargetCoordinatesResponse(
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().specificGravity().asBigDecimal(),
                CanonicalUnitLabels.SPECIFIC_GRAVITY
            ),
            new DecimalQuantityResponse(
                distance.nearestTargetPoint().titratableAcidity().asBigDecimal(),
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            )
        ),
        tanninQuantity(distance.tannin()),
        tanninQuantity(distance.nearestTargetTannin()),
        ProfileTargetObjectiveFunctionCode.valueOf(distance.objectiveFunction().name()),
        ProfileDistanceMetric.valueOf(distance.metric().name()),
        SugarAcidityTargetKind.valueOf(distance.targetKind().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape
            .valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis
                .valueOf(axis.name()))
            .sorted(Comparator.comparing(Enum::name))
            .toList()
    );
  }

  private static @Nullable DecimalQuantityResponse tanninQuantity(
      @Nullable TanninValue value
  ) {
    return value == null
        ? null
        : new DecimalQuantityResponse(value.asBigDecimal(), CanonicalUnitLabels.TANNIN);
  }
}
