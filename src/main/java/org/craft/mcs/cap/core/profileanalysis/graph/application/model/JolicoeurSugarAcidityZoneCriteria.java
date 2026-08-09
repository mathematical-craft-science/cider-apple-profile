package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Optional application filter restricting results to selected Jolicoeur sugar-acidity zones. */
public record JolicoeurSugarAcidityZoneCriteria(
    @NonNull Set<@NonNull JolicoeurSugarAcidityZone> zones
) {

  public JolicoeurSugarAcidityZoneCriteria {
    requireNonNull(zones, "Jolicoeur sugar-acidity zones cannot be null");
    zones = Set.copyOf(zones);

    if (zones.isEmpty()) {
      throw new ApplicationValidationException("Jolicoeur sugar-acidity zones cannot be empty");
    }
  }

  public static @NonNull JolicoeurSugarAcidityZoneCriteria of(
      @NonNull JolicoeurSugarAcidityZone zone
  ) {
    requireNonNull(zone, "Jolicoeur sugar-acidity zone cannot be null");
    return new JolicoeurSugarAcidityZoneCriteria(Set.of(zone));
  }

  public boolean matches(@NonNull JolicoeurSugarAcidityZoneAssessment assessment) {
    final var zone = requireNonNull(
        assessment,
        "Jolicoeur sugar-acidity zone assessment cannot be null"
    ).zone();
    return zone != null && zones.contains(zone);
  }
}
