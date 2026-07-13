package org.craft.mcs.cap.core.account.application.port.in;

import org.jspecify.annotations.NonNull;


/** Returns the already provisioned current CAP account without changing persisted state. */
public interface GetCurrentAccountUseCase {

  @NonNull AccountMeView get();
}
