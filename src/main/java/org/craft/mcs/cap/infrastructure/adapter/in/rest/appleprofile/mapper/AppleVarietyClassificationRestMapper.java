package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassification;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.BorderlineEvidenceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class AppleVarietyClassificationRestMapper {

  private AppleVarietyClassificationRestMapper() {
  }

  public static @NonNull AppleVarietyClassificationResponse toResponse(
      @NonNull AppleVarietyClassification classification
  ) {
    requireNonNull(classification, "apple variety classification cannot be null");

    final var result = classification.result();

    return new AppleVarietyClassificationResponse(
        classification.varietyId().value().toString(),
        classification.varietyName().value(),
        result.profileCode(),
        result.classified(),
        Optional.ofNullable(result.appleClass()).map(Enum::name).orElse(null),
        result.confidence(),
        result.borderline(),
        result.borderlineEvidence()
            .stream()
            .map(evidence -> new BorderlineEvidenceResponse(
                evidence.axis().name(),
                evidence.reason().name()
            ))
            .toList()
    );
  }
}