package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageClassification;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


/** Authoritative vessel catalog representation returned by Vessel endpoints. */
@Schema(description = "Workspace vessel catalog entry enriched with its current material usage.")
public record VesselResponse(
    @Schema(
        description = "Stable UUIDv7 vessel identifier referenced by material batches.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7e",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID vesselId,
    @Schema(
        description = "Workspace that owns the vessel.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Current vessel name.",
        example = "Fermentation tank F-03",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Nominal vessel capacity in canonical volume units.",
        implementation = DecimalQuantityResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse nominalCapacity,
    @Schema(
        description = "Vessel construction material.",
        implementation = VesselMaterial.class,
        example = "STAINLESS_STEEL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselMaterial material,
    @Schema(
        description = "Current operational availability.",
        implementation = VesselOperationalStatus.class,
        example = "OPERATIONAL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselOperationalStatus operationalStatus,
    @Schema(
        description = "Single active material batch currently occupying the vessel, or null when empty.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable VesselCurrentContentResponse currentContent,
    @Schema(
        description = "Volume currently occupied by active material; zero when the vessel is empty.",
        implementation = DecimalQuantityResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse usedVolume,
    @Schema(
        description = "Capacity remaining after subtracting current material volume from nominal capacity.",
        implementation = DecimalQuantityResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse remainingCapacity,
    @Schema(
        description = "Derived vessel capacity usage classification.",
        implementation = VesselUsageClassification.class,
        example = "PARTIALLY_FILLED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselUsageClassification usage,
    @Schema(
        description = "Optimistic-locking revision required by the next update.",
        format = "int64",
        example = "2",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long version
) {
}
