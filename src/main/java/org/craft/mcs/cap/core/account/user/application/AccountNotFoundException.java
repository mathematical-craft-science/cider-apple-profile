package org.craft.mcs.cap.core.account.user.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;

/** Raised when an authenticated external identity has no resolvable CAP account. */
@SuppressWarnings("java:S110")
public final class AccountNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AccountNotFoundException() {
    super("CAP account not found for the authenticated actor");
  }
}
