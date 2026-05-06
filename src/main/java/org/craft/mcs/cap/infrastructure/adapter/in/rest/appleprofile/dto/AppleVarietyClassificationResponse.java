package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;


import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationConfidence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record AppleVarietyClassificationResponse(
    @NonNull String varietyId,
    @NonNull String varietyName,
    @NonNull String profileCode,
    boolean classified,
    @Nullable String appleClass,
    @NonNull ClassificationConfidence confidence,
    boolean borderline,
    @NonNull List<@NonNull BorderlineEvidenceResponse> borderlineReasons
) {
}
