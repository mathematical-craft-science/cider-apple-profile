package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.util.List;
import org.jspecify.annotations.NonNull;


public record ChemicalProfileCapabilityAssessmentResponse(
    @NonNull String capability,
    boolean capable,
    @NonNull List<@NonNull String> requiredAxes,
    @NonNull List<@NonNull String> presentAxes,
    @NonNull List<@NonNull String> missingAxes
) {
}
