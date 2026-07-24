package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = """
        One immutable observation or working result. Repeated observations remain separate records and a derived
        RESULT references them through derivationInputs.
        """
)
public record MeasurementRecordResponse(
    @Schema(
        description = "Stable identifier of this immutable evidence record.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(
        description = "Material or process characteristic represented by this record.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "OBSERVATION preserves one reading; RESULT is the value used by current projections.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementRecordRole role,
    @Schema(
        description = "Canonical point or interval value retained by this record.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueResponse value,
    @Schema(
        description = "Time associated with the measurement evidence, not the revision creation time.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Source and confidence evidence attached to this record.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceResponse provenance,
    @Schema(
        description = "Derivation used by a calculated RESULT; null for direct results and observations.",
        nullable = true
    )
    @Nullable MeasurementDerivationMethod derivationMethod,
    @ArraySchema(
        arraySchema = @Schema(
            description = """
                Record identifiers of same-characteristic OBSERVATION inputs. Empty for direct results and
                observations.
                """,
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(format = "uuid")
    )
    @NonNull List<@NonNull UUID> derivationInputs
) {
}
