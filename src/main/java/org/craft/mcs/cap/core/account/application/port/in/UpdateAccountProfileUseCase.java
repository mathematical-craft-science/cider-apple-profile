package org.craft.mcs.cap.core.account.application.port.in;

import org.jspecify.annotations.NonNull;


/** Updates profile fields owned by CAP rather than the external identity provider. */
public interface UpdateAccountProfileUseCase {

  @NonNull AccountUserView update(@NonNull UpdateAccountProfileCommand command);
}
