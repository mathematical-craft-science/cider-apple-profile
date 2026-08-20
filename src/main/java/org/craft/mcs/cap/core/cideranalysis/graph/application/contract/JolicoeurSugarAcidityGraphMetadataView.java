package org.craft.mcs.cap.core.cideranalysis.graph.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable published metadata required to render the Jolicoeur SG/TA graph. */
public record JolicoeurSugarAcidityGraphMetadataView(
    @NonNull SpecificGravityAxis specificGravity,
    @NonNull TitratableAcidityAxis titratableAcidity,
    @NonNull List<@NonNull ZoneGeometry> zones
) {

  public JolicoeurSugarAcidityGraphMetadataView {
    requireNonNull(specificGravity, "specific gravity graph axis cannot be null");
    requireNonNull(titratableAcidity, "titratable acidity graph axis cannot be null");
    zones = List.copyOf(requireNonNull(zones, "graph zone geometries cannot be null"));
  }

  public record SpecificGravityAxis(
      @NonNull SpecificGravityValue min,
      @NonNull SpecificGravityValue max,
      @NonNull List<@NonNull SpecificGravityValue> boundaries
  ) {

    public SpecificGravityAxis {
      requireNonNull(min, "specific gravity graph minimum cannot be null");
      requireNonNull(max, "specific gravity graph maximum cannot be null");
      boundaries = List.copyOf(requireNonNull(boundaries, "specific gravity boundaries cannot be null"));
    }
  }

  public record TitratableAcidityAxis(
      @NonNull TitratableAcidityValue min,
      @NonNull TitratableAcidityValue max,
      @NonNull List<@NonNull TitratableAcidityValue> boundaries
  ) {

    public TitratableAcidityAxis {
      requireNonNull(min, "titratable acidity graph minimum cannot be null");
      requireNonNull(max, "titratable acidity graph maximum cannot be null");
      boundaries = List.copyOf(requireNonNull(boundaries, "titratable acidity boundaries cannot be null"));
    }
  }

  public sealed interface ZoneGeometry permits RectangleZone, EllipseZone {

    @NonNull JolicoeurSugarAcidityZoneCode zone();
  }

  public record RectangleZone(
      @NonNull JolicoeurSugarAcidityZoneCode zone,
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
  ) implements ZoneGeometry {

    public RectangleZone {
      requireNonNull(zone, "rectangle graph zone cannot be null");
      requireNonNull(specificGravity, "rectangle specific gravity range cannot be null");
      requireNonNull(titratableAcidity, "rectangle titratable acidity range cannot be null");
    }
  }

  public record EllipseZone(
      @NonNull JolicoeurSugarAcidityZoneCode zone,
      @NonNull SpecificGravityValue specificGravityCenter,
      @NonNull TitratableAcidityValue titratableAcidityCenter,
      @NonNull BigDecimal specificGravityRadius,
      @NonNull BigDecimal titratableAcidityRadius
  ) implements ZoneGeometry {

    public EllipseZone {
      requireNonNull(zone, "ellipse graph zone cannot be null");
      requireNonNull(specificGravityCenter, "ellipse specific gravity center cannot be null");
      requireNonNull(titratableAcidityCenter, "ellipse titratable acidity center cannot be null");
      requireNonNull(specificGravityRadius, "ellipse specific gravity radius cannot be null");
      requireNonNull(titratableAcidityRadius, "ellipse titratable acidity radius cannot be null");
    }
  }

}
