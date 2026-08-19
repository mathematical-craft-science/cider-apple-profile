package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.result.BorderlineEvidence;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.result.ClassificationConfidence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record AppleVarietyClassification(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName varietyName,
    @NonNull String profileCode,
    boolean classified,
    @Nullable String appleClass,
    @NonNull ClassificationConfidence confidence,
    boolean borderline,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
) {
}
