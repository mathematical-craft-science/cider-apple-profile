package org.craft.mcs.cap.core.account.user.application.port.in;

import org.jspecify.annotations.NonNull;

/** Returns the CAP-owned profile of the current authenticated account. */
public interface GetCurrentAccountProfileUseCase {

  @NonNull AccountUserView get();
}
