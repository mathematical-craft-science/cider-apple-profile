package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV prediction response for a feedstock batch current profile.")
public record FeedstockBatchCurrentPotentialAbvResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Current feedstock measurement identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18") @NonNull UUID currentMeasurementId,
    @Schema(description = "Current measurement name.", example = "Pressing measurement #1") @NonNull String currentMeasurementName,
    @Schema(description = "Source value kind used for original gravity.", implementation = MeasurementValueKind.class, example = "EXACT")
    @NonNull MeasurementValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.")
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
