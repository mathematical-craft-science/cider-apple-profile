package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedZoneProfileTargetRequest(
    @NonNull String kind,
    @NonNull SugarAcidityZone zone
) implements ProfileTargetRequest {

  public PredefinedZoneProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
