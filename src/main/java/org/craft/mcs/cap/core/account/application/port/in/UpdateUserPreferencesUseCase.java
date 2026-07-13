package org.craft.mcs.cap.core.account.application.port.in;

import org.jspecify.annotations.NonNull;


/** Updates CAP user presentation preferences. */
public interface UpdateUserPreferencesUseCase {

  @NonNull UserPreferencesView update(@NonNull UpdateUserPreferencesCommand command);
}
