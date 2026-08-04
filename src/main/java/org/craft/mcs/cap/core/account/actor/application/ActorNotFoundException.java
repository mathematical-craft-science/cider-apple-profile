package org.craft.mcs.cap.core.account.actor.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;


/** Raised when a durable actor reference cannot be resolved by the CAP actor directory. */
@SuppressWarnings("java:S110")
public final class ActorNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ActorNotFoundException(@NonNull ActorReference actor) {
    super("CAP actor not found: " + actor.type() + "/" + actor.id());
  }
}
