package org.craft.mcs.cap.core.account.membership.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.jspecify.annotations.NonNull;

/** Raised when the authenticated actor has no active membership or required CAP permission. */
@SuppressWarnings("java:S110")
public final class AccountAccessDeniedException extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AccountAccessDeniedException(@NonNull String message) {
    super(message);
  }
}
