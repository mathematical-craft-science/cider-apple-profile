package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorLifecycleStatus;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Minimal provider-neutral actor identity returned for audit and history presentation. */
@Schema(description = "Display identity of a human or machine author referenced by CAP history.")
public record ActorDisplayResponse(
    @Schema(
        description = "Actor category stored with the auditable action.",
        implementation = ActorType.class,
        example = "ACCOUNT_USER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ActorType type,
    @Schema(
        description = "Stable CAP-owned actor identifier.",
        format = "uuid",
        example = "019f818a-7008-7000-9ef5-3381945e3c41",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID id,
    @Schema(
        description = "Display name suitable for audit and operation history.",
        example = "Paul Ovchinnikov",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String displayName,
    @Schema(
        description = "Optional absolute avatar URL. Machine identities and deleted users may not have one.",
        format = "uri",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String avatarUrl,
    @Schema(
        description = "Current actor lifecycle state. Deleted actors remain resolvable as anonymized tombstones.",
        implementation = ActorLifecycleStatus.class,
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ActorLifecycleStatus status
) {
}
