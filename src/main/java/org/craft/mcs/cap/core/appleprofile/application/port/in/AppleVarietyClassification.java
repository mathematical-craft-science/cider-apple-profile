package org.craft.mcs.cap.core.appleprofile.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.classification.result.BorderlineEvidence;
import org.craft.mcs.cap.shared.domain.classification.result.ClassificationConfidence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record AppleVarietyClassification(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull String profileCode,
    boolean classified,
    @Nullable String appleClass,
    @NonNull ClassificationConfidence confidence,
    boolean borderline,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
) {
}
