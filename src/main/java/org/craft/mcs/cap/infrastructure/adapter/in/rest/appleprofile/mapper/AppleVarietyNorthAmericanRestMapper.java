package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyNorthAmericanResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class AppleVarietyNorthAmericanRestMapper {

  private AppleVarietyNorthAmericanRestMapper() {
  }

  public static @NonNull AppleVarietyNorthAmericanResponse toResponse(
      @NonNull AppleVarietyNorthAmericanClassification classification
  ) {
    requireNonNull(classification);

    final var profile = classification.profile();

    return new AppleVarietyNorthAmericanResponse(
        classification.varietyId().value(),
        classification.varietyName().value(),
        profile.sugar(),
        profile.acidity(),
        profile.tannin()
    );
  }
}
