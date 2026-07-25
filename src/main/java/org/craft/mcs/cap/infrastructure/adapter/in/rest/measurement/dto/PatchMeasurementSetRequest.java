package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Applies a partial correction by copying unchanged evidence into a new immutable revision."
)
public record PatchMeasurementSetRequest(
    @Schema(
        description = "Optimistic-locking version returned by the latest read of the logical set.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long expectedVersion,
    @Schema(
        description = "Replacement name. When omitted, the current name is copied to the new revision.",
        minLength = 1,
        maxLength = 160,
        nullable = true
    )
    @Nullable String name,
    @Schema(
        description = "Whether note is part of the patch. False preserves the current note; true applies note."
    )
    boolean noteSpecified,
    @Schema(
        description = "Replacement note when noteSpecified is true. A null value clears the current note.",
        maxLength = 2000,
        nullable = true
    )
    @Nullable String note,
    @Schema(
        description = "Required explanation of why the previous revision is being corrected.",
        minLength = 1,
        maxLength = 1000,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String correctionReason,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Characteristics whose complete evidence replaces the current evidence.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristicRequest.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristicRequest> replacements,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Characteristics excluded from the new revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristic.class)
    )
    @NonNull Set<@NonNull MeasurementCharacteristic> removals
) {
}
