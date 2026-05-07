package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphAxisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphRangeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphAxis;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.EllipseSugarAcidityGraphZone;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.RectangleSugarAcidityGraphZone;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphRange;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphZone;
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
      case RectangleSugarAcidityGraphZone rectangle -> new RectangleSugarAcidityGraphZoneResponse(
          rectangle.zone().name(),
          rangeResponse(rectangle.sugarRange()),
          rangeResponse(rectangle.acidityRange())
      );
      case EllipseSugarAcidityGraphZone ellipse -> new EllipseSugarAcidityGraphZoneResponse(
          ellipse.zone().name(),
          ellipse.sugarCenter(),
          ellipse.acidityCenter(),
          ellipse.sugarRadius(),
          ellipse.acidityRadius()
      );
    };
  }

  private static @NonNull SugarAcidityGraphRangeResponse rangeResponse(
      @NonNull SugarAcidityGraphRange range
  ) {
    requireNonNull(range, "sugar-acidity graph range cannot be null");

    return new SugarAcidityGraphRangeResponse(
        range.min(),
        range.max()
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
        axis.min(),
        axis.max(),
        axis.thresholdLines()
    );
  }
}
