package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphAxisScale;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Ordered zone geometry and axis boundaries of the Jolicoeur SG/TA interpretation.
 *
 * <p>Order is significant when configured geometries share a boundary: the first matching zone
 * wins.</p>
 */
public record JolicoeurSugarAcidityZoneGeometryProfile(
    @NonNull List<@NonNull JolicoeurSugarAcidityZoneGeometry> zones,
    @NonNull SugarAcidityGraphAxisScale<SpecificGravityValue> specificGravityScale,
    @NonNull SugarAcidityGraphAxisScale<TitratableAcidityValue> titratableAcidityScale
) {

  public JolicoeurSugarAcidityZoneGeometryProfile {
    zones = List.copyOf(requireNonNull(zones, "sugar-acidity zone geometries cannot be null"));
    requireNonNull(specificGravityScale, "specific gravity graph scale cannot be null");
    requireNonNull(titratableAcidityScale, "titratable acidity graph scale cannot be null");
    if (zones.isEmpty()) {
      throw new DomainRuleViolationException(
          "sugar-acidity zone geometry profile must contain at least one zone"
      );
    }
    final var distinctZoneCount = zones.stream()
        .map(JolicoeurSugarAcidityZoneGeometry::zone)
        .distinct()
        .count();
    if (distinctZoneCount != zones.size()) {
      throw new DomainRuleViolationException(
          "sugar-acidity zone geometry profile cannot contain duplicate zones"
      );
    }
  }

  public @NonNull Optional<@NonNull JolicoeurSugarAcidityZoneGeometry> find(
      @NonNull JolicoeurSugarAcidityZoneId zone
  ) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return zones.stream()
        .filter(geometry -> geometry.zone() == zone)
        .findFirst();
  }
}
