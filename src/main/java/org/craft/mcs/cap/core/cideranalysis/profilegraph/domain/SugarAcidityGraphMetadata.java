package org.craft.mcs.cap.core.cideranalysis.profilegraph.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityGraphMetadata(
    @NonNull SugarAcidityGraphAxis sugarAxis,
    @NonNull SugarAcidityGraphAxis acidityAxis,
    @NonNull List<@NonNull SugarAcidityGraphZone> zones
) {

  public SugarAcidityGraphMetadata {
    requireNonNull(sugarAxis, "sugar graph axis cannot be null");
    requireNonNull(acidityAxis, "acidity graph axis cannot be null");
    zones = List.copyOf(requireNonNull(zones, "sugar-acidity graph zones cannot be null"));
  }
}
