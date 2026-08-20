package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphMetadataView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAxisResponse;
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


/** Maps published Jolicoeur graph metadata to its transport contract. */
public final class JolicoeurSugarAcidityGraphMetadataRestMapper {

  private JolicoeurSugarAcidityGraphMetadataRestMapper() {
  }

  public static @NonNull JolicoeurSugarAcidityGraphMetadataResponse toResponse(
      @NonNull JolicoeurSugarAcidityGraphMetadataView metadata
  ) {
    requireNonNull(metadata, "sugar-acidity graph metadata cannot be null");
    return new JolicoeurSugarAcidityGraphMetadataResponse(
        axisResponse(
            metadata.specificGravity().min(),
            metadata.specificGravity().max(),
            metadata.specificGravity().boundaries(),
            SpecificGravityValue::asBigDecimal,
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        axisResponse(
            metadata.titratableAcidity().min(),
            metadata.titratableAcidity().max(),
            metadata.titratableAcidity().boundaries(),
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        metadata.zones().stream()
            .map(JolicoeurSugarAcidityGraphMetadataRestMapper::zoneResponse)
            .toList()
    );
  }

  private static @NonNull SugarAcidityGraphZoneResponse zoneResponse(
      JolicoeurSugarAcidityGraphMetadataView.@NonNull ZoneGeometry geometry
  ) {
    return switch (geometry) {
      case JolicoeurSugarAcidityGraphMetadataView.RectangleZone(
          var zone,
          var specificGravity,
          var titratableAcidity
      ) -> new RectangleSugarAcidityGraphZoneResponse(
          JolicoeurSugarAcidityZone.valueOf(zone.name()),
          rangeResponse(specificGravity, CanonicalUnitLabels.SPECIFIC_GRAVITY),
          rangeResponse(titratableAcidity, CanonicalUnitLabels.TITRATABLE_ACIDITY)
      );
      case JolicoeurSugarAcidityGraphMetadataView.EllipseZone(
          var zone,
          var specificGravityCenter,
          var titratableAcidityCenter,
          var specificGravityRadius,
          var titratableAcidityRadius
      ) -> new EllipseSugarAcidityGraphZoneResponse(
          JolicoeurSugarAcidityZone.valueOf(zone.name()),
          quantity(specificGravityCenter.asBigDecimal(), CanonicalUnitLabels.SPECIFIC_GRAVITY),
          quantity(titratableAcidityCenter.asBigDecimal(), CanonicalUnitLabels.TITRATABLE_ACIDITY),
          quantity(specificGravityRadius, CanonicalUnitLabels.SPECIFIC_GRAVITY),
          quantity(titratableAcidityRadius, CanonicalUnitLabels.TITRATABLE_ACIDITY)
      );
    };
  }

  private static <V extends MeasuredValue> @NonNull SugarAcidityGraphRangeResponse rangeResponse(
      @NonNull MeasuredValueRange<V> range,
      @NonNull String unit
  ) {
    return new SugarAcidityGraphRangeResponse(
        range.min().asBigDecimal(),
        range.max().asBigDecimal(),
        unit
    );
  }

  private static <V> @NonNull SugarAcidityGraphAxisResponse axisResponse(
      @NonNull V min,
      @NonNull V max,
      @NonNull List<@NonNull V> boundaries,
      @NonNull Function<V, BigDecimal> decimalValue,
      @NonNull String unit
  ) {
    return new SugarAcidityGraphAxisResponse(
        unit,
        quantity(decimalValue.apply(min), unit),
        quantity(decimalValue.apply(max), unit),
        boundaries.stream()
            .map(decimalValue)
            .map(value -> quantity(value, unit))
            .toList()
    );
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }
}
