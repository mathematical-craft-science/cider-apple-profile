package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Sugar-acidity graph assessment for an apple variety.")
public record AppleVarietySugarAcidityZoneResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f") @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black") @NonNull String varietyName,
    @Schema(description = "Whether the variety can be plotted.") boolean plottable,
    @Schema(description = "Zone assessment when plottable.") @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(description = "Capability assessment explaining missing measurements.") @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {

  public AppleVarietySugarAcidityZoneResponse(
      @NonNull UUID varietyId,
      @NonNull String varietyName,
      @NonNull SugarAcidityZoneAssessmentResponse assessment
  ) {
    this(varietyId, varietyName, true, assessment, null);
  }
}
