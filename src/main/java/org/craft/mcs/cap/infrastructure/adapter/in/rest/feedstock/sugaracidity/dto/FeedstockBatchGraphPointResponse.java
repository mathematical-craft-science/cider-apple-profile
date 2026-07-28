package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
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
        description = "Material state from which the composite current profile was projected.",
        format = "uuid",
        example = "019fb533-6d2e-7000-8f1c-b64f74d52079",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Latest measurement set contributing evidence to the composite current profile.",
        format = "uuid",
        example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMeasurementId,
    @Schema(
        description = "Timestamp of the latest measurement contributing evidence to the composite current profile.",
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
    @Nullable SugarAcidityGraphPointResponse point,
    @Schema(
        description = "Jolicoeur sugar-acidity zone code, when classified.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional North American tannin level used to style the graph point. "
            + "It does not affect SG/TA coordinates or Jolicoeur zone classification.",
        implementation = NorthAmericanTanninLevel.class,
        example = "MEDIUM",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
    @ArraySchema(
        arraySchema = @Schema(
            description = "SG/TA characteristics missing for graph plotting. Empty when plottable.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = GraphMissingCharacteristic.class)
    )
    @NonNull List<@NonNull GraphMissingCharacteristic> missingAxes
) {
}
