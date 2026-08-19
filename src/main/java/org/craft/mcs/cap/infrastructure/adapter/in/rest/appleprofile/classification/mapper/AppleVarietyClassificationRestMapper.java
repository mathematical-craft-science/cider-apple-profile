package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.mapper;

import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.AppleVarietyClassification;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.BorderlineEvidenceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class AppleVarietyClassificationRestMapper {

  private AppleVarietyClassificationRestMapper() {
  }

  public static @NonNull AppleVarietyClassificationResponse toResponse(
      @NonNull AppleVarietyClassification classification
  ) {
    requireNonNull(classification, "apple variety classification cannot be null");

    return new AppleVarietyClassificationResponse(
        classification.varietyId().value(),
        classification.varietyName().value(),
        classification.profileCode(),
        classification.classified(),
        classification.appleClass(),
        classification.confidence(),
        classification.borderline(),
        classification.borderlineEvidence()
            .stream()
            .map(evidence -> new BorderlineEvidenceResponse(
                evidence.characteristic().name(),
                evidence.reason().name()
            ))
            .toList()
    );
  }
}
