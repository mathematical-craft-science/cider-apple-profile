package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock batch graph point based on the state-aware current profile.")
public record FeedstockBatchGraphPointResponse(
    @Schema(
        description = "Feedstock batch identifier.",
        format = "uuid",
        example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID batchId,
    @Schema(
        description = "Feedstock batch name.",
        example = "Late season juice",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String batchName,
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Current feedstock measurement identifier.",
        format = "uuid",
        example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMeasurementId,
    @Schema(
        description = "Measurement timestamp used as the current batch state.",
        format = "date-time",
        example = "2026-09-28T10:15:30Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Whether the current batch profile has enough data to be plotted.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Computed graph point coordinates, when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityProfilePointResponse point,
    @Schema(
        description = "Sugar-acidity zone code, when classified.",
        implementation = SugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityZone zone,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Chemical axes missing for graph plotting. Empty when plottable.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = ChemicalAxis.class)
    )
    @NonNull List<@NonNull ChemicalAxis> missingAxes,
    @Schema(
        description = "Zone assessment, when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(
        description = "Capability assessment, present when the current batch profile is not plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
