package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.mapper;

import org.craft.mcs.cap.core.account.actor.application.model.ActorDisplayView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.dto.ActorDisplayResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps provider-neutral actor directory views to REST responses. */
public final class ActorDirectoryRestMapper {

  private ActorDirectoryRestMapper() {
  }

  public static @NonNull ActorDisplayResponse toResponse(@NonNull ActorDisplayView view) {
    requireNonNull(view, "actor display view cannot be null");
    return new ActorDisplayResponse(
        view.actor().type(),
        view.actor().id(),
        view.displayName().value(),
        view.avatarUrl() == null ? null : view.avatarUrl().value().toString(), // NOSONAR
        view.status()
    );
  }
}
