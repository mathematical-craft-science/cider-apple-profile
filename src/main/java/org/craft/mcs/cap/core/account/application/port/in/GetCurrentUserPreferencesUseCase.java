package org.craft.mcs.cap.core.account.application.port.in;

import org.jspecify.annotations.NonNull;


/** Returns personal preferences of the current authenticated CAP user. */
public interface GetCurrentUserPreferencesUseCase {

  @NonNull UserPreferencesView get();
}
