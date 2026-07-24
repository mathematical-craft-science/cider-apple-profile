package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.EllipseSugarAcidityGraphZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.RectangleSugarAcidityGraphZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphAxis;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphRange;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphAxisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphRangeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SugarAcidityGraphMetadataRestMapper {

  private SugarAcidityGraphMetadataRestMapper() {
  }

  public static @NonNull SugarAcidityGraphMetadataResponse toResponse(
      @NonNull SugarAcidityGraphMetadata metadata
  ) {
    requireNonNull(metadata, "sugar-acidity graph metadata cannot be null");

    return new SugarAcidityGraphMetadataResponse(
        axisResponse(metadata.sugarAxis()),
        axisResponse(metadata.acidityAxis()),
        metadata.zones().stream()
            .map(SugarAcidityGraphMetadataRestMapper::zoneResponse)
            .toList()
    );
  }

  private static @NonNull SugarAcidityGraphZoneResponse zoneResponse(
      @NonNull SugarAcidityGraphZone zone
  ) {
    requireNonNull(zone, "sugar-acidity graph zone cannot be null");

    return switch (zone) {
      case RectangleSugarAcidityGraphZone(var zoneCode, var sugarRange, var acidityRange) ->
          rectangleZoneResponse(zoneCode, sugarRange, acidityRange);
      case EllipseSugarAcidityGraphZone(
          var zoneCode,
          var sugarCenter,
          var acidityCenter,
          var sugarRadius,
          var acidityRadius
      ) -> ellipseZoneResponse(
          zoneCode,
          sugarCenter,
          acidityCenter,
          sugarRadius,
          acidityRadius
      );
    };
  }

  private static @NonNull RectangleSugarAcidityGraphZoneResponse rectangleZoneResponse(
      @NonNull SugarAcidityZone zone,
      @NonNull SugarAcidityGraphRange sugarRange,
      @NonNull SugarAcidityGraphRange acidityRange
  ) {
    return new RectangleSugarAcidityGraphZoneResponse(
        zone,
        rangeResponse(sugarRange, CanonicalUnitLabels.SUGAR),
        rangeResponse(acidityRange, CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @NonNull EllipseSugarAcidityGraphZoneResponse ellipseZoneResponse(
      @NonNull SugarAcidityZone zone,
      @NonNull BigDecimal sugarCenter,
      @NonNull BigDecimal acidityCenter,
      @NonNull BigDecimal sugarRadius,
      @NonNull BigDecimal acidityRadius
  ) {
    return new EllipseSugarAcidityGraphZoneResponse(
        zone,
        new DecimalQuantityResponse(sugarCenter, CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(acidityCenter, CanonicalUnitLabels.TITRATABLE_ACIDITY),
        new DecimalQuantityResponse(sugarRadius, CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(acidityRadius, CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @NonNull SugarAcidityGraphRangeResponse rangeResponse(
      @NonNull SugarAcidityGraphRange range,
      @NonNull String unit
  ) {
    requireNonNull(range, "sugar-acidity graph range cannot be null");
    requireNonNull(unit, "sugar-acidity graph range unit cannot be null");

    return new SugarAcidityGraphRangeResponse(
        range.min(),
        range.max(),
        unit
    );
  }

  private static @NonNull SugarAcidityGraphAxisResponse axisResponse(
      @NonNull SugarAcidityGraphAxis axis
  ) {
    requireNonNull(axis, "sugar-acidity graph axis cannot be null");

    return new SugarAcidityGraphAxisResponse(
        axis.code(),
        axis.label(),
        axis.unit(),
        new DecimalQuantityResponse(axis.min(), axis.unit()),
        new DecimalQuantityResponse(axis.max(), axis.unit()),
        axis.thresholdLines().stream()
            .map(value -> new DecimalQuantityResponse(value, axis.unit()))
            .toList()
    );
  }
}
