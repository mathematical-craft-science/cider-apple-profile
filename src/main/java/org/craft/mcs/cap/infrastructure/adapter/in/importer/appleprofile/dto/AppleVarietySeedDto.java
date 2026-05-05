package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

import org.craft.mcs.cap.shared.domain.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record AppleVarietySeedDto(
    @NonNull String name,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence,
    @Nullable MeasurementSeedDto sugar,
    @Nullable MeasurementSeedDto ph,
    @Nullable MeasurementSeedDto titratableAcidity,
    @Nullable MeasurementSeedDto tanninity
) {
}
