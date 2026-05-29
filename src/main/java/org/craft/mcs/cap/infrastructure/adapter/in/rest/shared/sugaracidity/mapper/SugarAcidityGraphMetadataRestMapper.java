package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphAxisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphRangeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphAxis;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.EllipseSugarAcidityGraphZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.RectangleSugarAcidityGraphZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphRange;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphZone;
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
      case RectangleSugarAcidityGraphZone rectangle -> rectangleZoneResponse(rectangle);
      case EllipseSugarAcidityGraphZone ellipse -> ellipseZoneResponse(ellipse);
    };
  }

  private static @NonNull RectangleSugarAcidityGraphZoneResponse rectangleZoneResponse(
      @NonNull RectangleSugarAcidityGraphZone rectangle
  ) {
    return new RectangleSugarAcidityGraphZoneResponse(
        rectangle.zone(),
        rangeResponse(rectangle.sugarRange(), CanonicalUnitLabels.SUGAR),
        rangeResponse(rectangle.acidityRange(), CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @NonNull EllipseSugarAcidityGraphZoneResponse ellipseZoneResponse(
      @NonNull EllipseSugarAcidityGraphZone ellipse
  ) {
    return new EllipseSugarAcidityGraphZoneResponse(
        ellipse.zone(),
        new DecimalQuantityResponse(ellipse.sugarCenter(), CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(ellipse.acidityCenter(), CanonicalUnitLabels.TITRATABLE_ACIDITY),
        new DecimalQuantityResponse(ellipse.sugarRadius(), CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(ellipse.acidityRadius(), CanonicalUnitLabels.TITRATABLE_ACIDITY)
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
