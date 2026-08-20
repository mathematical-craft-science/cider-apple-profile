package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.mapper;

import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyNorthAmericanResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanAcidityLevel;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanSugarLevel;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
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
    final var sugar = requireNonNull(profile.sugar(), "North American sugar level cannot be null");
    final var acidity = requireNonNull(profile.acidity(), "North American acidity level cannot be null");
    final var tannin = requireNonNull(profile.tannin(), "North American tannin level cannot be null");

    return new AppleVarietyNorthAmericanResponse(
        classification.varietyId().value(),
        classification.varietyName().value(),
        NorthAmericanSugarLevel.valueOf(sugar.name()),
        NorthAmericanAcidityLevel.valueOf(acidity.name()),
        NorthAmericanTanninLevel.valueOf(tannin.name())
    );
  }
}
