package org.craft.mcs.cap.core.account.user.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.jspecify.annotations.NonNull;

/** Supplies the authenticated actor without exposing the security provider or framework to core. */
public interface CurrentActorProvider {

  @NonNull Optional<@NonNull AuthenticatedActor> currentActor();
}
