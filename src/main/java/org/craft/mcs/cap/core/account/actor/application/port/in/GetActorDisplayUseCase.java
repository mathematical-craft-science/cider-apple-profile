package org.craft.mcs.cap.core.account.actor.application.port.in;

import org.craft.mcs.cap.core.account.actor.application.contract.ActorDisplayView;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;


/** Resolves one durable actor reference for audit and history presentation. */
public interface GetActorDisplayUseCase {

  @NonNull ActorDisplayView get(@NonNull ActorReference actor);
}
