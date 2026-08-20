package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Result of applying the Jolicoeur zone interpretation to an SG/TA graph point.
 *
 * <p>The result is not a canonical level of sugar or acidity.</p>
 */
public record JolicoeurSugarAcidityZoneAssessment(
    @Nullable JolicoeurSugarAcidityZoneId zone
) {

  public static @NonNull JolicoeurSugarAcidityZoneAssessment classified(
      @NonNull JolicoeurSugarAcidityZoneId zone
  ) {
    return new JolicoeurSugarAcidityZoneAssessment(
        requireNonNull(zone, "sugar-acidity zone cannot be null")
    );
  }

  public static @NonNull JolicoeurSugarAcidityZoneAssessment unclassified() {
    return new JolicoeurSugarAcidityZoneAssessment(null);
  }

  public boolean classified() {
    return zone != null;
  }
}
