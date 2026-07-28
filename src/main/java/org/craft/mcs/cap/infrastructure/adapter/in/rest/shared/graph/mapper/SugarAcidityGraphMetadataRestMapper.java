package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphAxisScale;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.EllipseSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.RectangleSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAxisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphRangeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SugarAcidityGraphMetadataRestMapper {

  private SugarAcidityGraphMetadataRestMapper() {
  }

  public static @NonNull SugarAcidityGraphMetadataResponse toResponse(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    requireNonNull(geometryProfile, "sugar-acidity zone geometry profile cannot be null");

    return new SugarAcidityGraphMetadataResponse(
        axisResponse(
            geometryProfile.specificGravityScale(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        axisResponse(
            geometryProfile.titratableAcidityScale(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        geometryProfile.zones().stream()
            .map(SugarAcidityGraphMetadataRestMapper::zoneResponse)
            .toList()
    );
  }

  private static @NonNull SugarAcidityGraphZoneResponse zoneResponse(
      @NonNull JolicoeurSugarAcidityZoneGeometry geometry
  ) {
    requireNonNull(geometry, "sugar-acidity zone geometry cannot be null");

    return switch (geometry) {
      case RectangleSugarAcidityZoneGeometry(
          var zone,
          var rectangle
      ) ->
          rectangleZoneResponse(
              zone,
              rectangle.specificGravityRange(),
              rectangle.titratableAcidityRange()
          );
      case EllipseSugarAcidityZoneGeometry(
          var zone,
          var ellipse
      ) -> ellipseZoneResponse(
          zone,
          ellipse.specificGravityCenter(),
          ellipse.titratableAcidityCenter(),
          ellipse.specificGravityRadius(),
          ellipse.titratableAcidityRadius()
      );
    };
  }

  private static @NonNull RectangleSugarAcidityGraphZoneResponse rectangleZoneResponse(
      @NonNull JolicoeurSugarAcidityZone zone,
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange
  ) {
    return new RectangleSugarAcidityGraphZoneResponse(
        zone,
        rangeResponse(specificGravityRange, CanonicalUnitLabels.SPECIFIC_GRAVITY),
        rangeResponse(titratableAcidityRange, CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @NonNull EllipseSugarAcidityGraphZoneResponse ellipseZoneResponse(
      @NonNull JolicoeurSugarAcidityZone zone,
      @NonNull SpecificGravityValue specificGravityCenter,
      @NonNull TitratableAcidityValue titratableAcidityCenter,
      @NonNull BigDecimal specificGravityRadius,
      @NonNull BigDecimal titratableAcidityRadius
  ) {
    return new EllipseSugarAcidityGraphZoneResponse(
        zone,
        new DecimalQuantityResponse(
            specificGravityCenter.asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        new DecimalQuantityResponse(
            titratableAcidityCenter.asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        new DecimalQuantityResponse(specificGravityRadius, CanonicalUnitLabels.SPECIFIC_GRAVITY),
        new DecimalQuantityResponse(titratableAcidityRadius, CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static <V extends MeasuredValue> @NonNull SugarAcidityGraphRangeResponse rangeResponse(
      @NonNull MeasuredValueRange<V> range,
      @NonNull String unit
  ) {
    requireNonNull(range, "sugar-acidity graph range cannot be null");
    requireNonNull(unit, "sugar-acidity graph range unit cannot be null");

    return new SugarAcidityGraphRangeResponse(
        range.min().asBigDecimal(),
        range.max().asBigDecimal(),
        unit
    );
  }

  private static <V extends MeasuredValue> @NonNull SugarAcidityGraphAxisResponse axisResponse(
      @NonNull SugarAcidityGraphAxisScale<V> scale,
      @NonNull String unit
  ) {
    requireNonNull(scale, "sugar-acidity graph axis scale cannot be null");
    requireNonNull(unit, "sugar-acidity graph axis unit cannot be null");

    return new SugarAcidityGraphAxisResponse(
        unit,
        new DecimalQuantityResponse(scale.min().asBigDecimal(), unit),
        new DecimalQuantityResponse(scale.max().asBigDecimal(), unit),
        scale.boundaryValues().stream()
            .map(MeasuredValue::asBigDecimal)
            .map(value -> new DecimalQuantityResponse(value, unit))
            .toList()
    );
  }
}
