package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock measurement sugar-acidity graph point and assessment response.")
public record FeedstockMeasurementSugarAcidityZoneResponse(
    @Schema(
        description = "Feedstock measurement identifier.",
        format = "uuid",
        example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementId,
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Feedstock batch identifier.",
        format = "uuid",
        example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID batchId,
    @Schema(
        description = "Measurement name.",
        example = "Pressing measurement #1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String measurementName,
    @Schema(
        description = "Whether the measurement has enough data to be plotted.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Zone assessment, when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityGraphAssessmentResponse assessment,
    @Schema(
        description = "Capability assessment, present when the sample is not plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {

  public FeedstockMeasurementSugarAcidityZoneResponse(
      @NonNull UUID measurementId,
      @NonNull UUID workspaceId,
      @NonNull UUID batchId,
      @NonNull String measurementName,
      @NonNull SugarAcidityGraphAssessmentResponse assessment
  ) {
    this(measurementId, workspaceId, batchId, measurementName, true, assessment, null);
  }
}
