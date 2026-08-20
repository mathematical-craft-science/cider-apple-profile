package org.craft.mcs.cap.core.account.actor.application.contract;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Minimal identity projection used to render authors in audit and operational history. */
public record ActorDisplayView(
    @NonNull ActorReference actor,
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl,
    @NonNull ActorLifecycleStatus status
) {

  public ActorDisplayView {
    requireNonNull(actor, "actor reference cannot be null");
    requireNonNull(displayName, "actor display name cannot be null");
    requireNonNull(status, "actor lifecycle status cannot be null");
  }
}
