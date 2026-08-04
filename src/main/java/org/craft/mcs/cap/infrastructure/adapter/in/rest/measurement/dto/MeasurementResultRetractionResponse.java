package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


/** Retained tombstone for one explicitly retracted characteristic result. */
@Schema(description = "Immutable evidence that a characteristic result was explicitly retracted.")
public record MeasurementResultRetractionResponse(
    @Schema(
        description = "Immutable record identity of the retracted RESULT evidence.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(
        description = "Characteristic whose working RESULT was retracted.",
        implementation = MeasurementCharacteristic.class,
        example = "PH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "Retained result status.",
        implementation = MeasurementResultRetractionStatus.class,
        example = "RETRACTED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementResultRetractionStatus status,
    @Schema(
        description = "Immutable snapshot of the retracted representative RESULT evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementRecordResponse resultSnapshot,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Immutable source observations used to derive the retracted representative result.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementRecordResponse.class)
    )
    @NonNull List<@NonNull MeasurementRecordResponse> observationSnapshots,
    @Schema(
        description = "Revision that last exposed the result as active evidence.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID sourceRevisionId,
    @Schema(
        description = "Number of the revision that last exposed the result as active evidence.",
        minimum = "1",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int sourceRevisionNumber,
    @Schema(
        description = "CAP actor that retracted the result.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementActorReferenceResponse actor,
    @Schema(
        description = "Time when the result was retracted.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant retractedAt,
    @Schema(
        description = "Reason supplied for retracting the result.",
        minLength = 1,
        maxLength = 1000,
        example = "Laboratory invalidated the pH result",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String reason
) {
}
