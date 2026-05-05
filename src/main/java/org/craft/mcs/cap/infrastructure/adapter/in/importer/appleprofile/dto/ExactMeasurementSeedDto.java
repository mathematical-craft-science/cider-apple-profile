package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ExactMeasurementSeedDto(
    @NonNull MeasurementSeedKind kind,
    @NonNull MeasurementSeedUnit unit,
    @NonNull String value
) implements MeasurementSeedDto {

  public ExactMeasurementSeedDto {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(value, "exact measurement value cannot be null");
  }
}
