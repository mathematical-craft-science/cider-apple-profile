package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.jspecify.annotations.NonNull;


public record AppleVarietyNorthAmericanResponse(
    @NonNull String varietyId,
    @NonNull String varietyName,
    @NonNull String sugar,
    @NonNull String acidity,
    @NonNull String tannin
) {
}
