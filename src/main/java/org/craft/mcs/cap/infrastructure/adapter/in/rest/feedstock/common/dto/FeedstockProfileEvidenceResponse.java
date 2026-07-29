package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = """
        Evidence selected for one characteristic of the composite current Feedstock profile. The response identifies
        the immutable measurement result supporting the value and describes its origin, provenance, and freshness.
        It does not contain the characteristic value itself.
        """
)
public record FeedstockProfileEvidenceResponse(
    @Schema(
        description = "Feedstock profile characteristic supported by this evidence.",
        implementation = MeasurementCharacteristic.class,
        example = "TITRATABLE_ACIDITY",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "How the selected value became applicable to the current material state.",
        implementation = MeasurementValueOrigin.class,
        example = "MEASURED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueOrigin origin,
    @Schema(
        description = "Material state to which the selected measurement evidence applies.",
        format = "uuid",
        example = "019f818a-7008-7000-9ef5-3381945e3c41",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID materialStateId,
    @Schema(
        description = "Stable identifier of the logical MeasurementSet containing the selected result.",
        format = "uuid",
        example = "019f818b-0134-7000-8af2-a9e37c4b1092",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementSetId,
    @Schema(
        description = "Identifier of the immutable RESULT record selected as current evidence.",
        format = "uuid",
        example = "019f818b-2631-7000-92a0-2cff5a3cb2fd",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(
        description = "Time at which the selected characteristic result was measured.",
        format = "date-time",
        example = "2026-07-25T16:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Human-readable provenance source; null means that no source name was declared.",
        example = "CAP Laboratory",
        minLength = 1,
        maxLength = 255,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = """
            Confidence in the provenance supporting the selected value; null means that confidence was not declared.
            This does not represent technological quality or numerical measurement uncertainty.
            """,
        implementation = ConfidenceLevel.class,
        example = "LAB_VERIFIED",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ConfidenceLevel confidence,
    @Schema(
        description = "Freshness of this characteristic relative to the active workspace freshness policy.",
        implementation = MeasurementFreshnessStatus.class,
        example = "CURRENT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @Schema(
        description = "Convenience flag equal to true when freshnessStatus is STALE.",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean stale
) {
}
