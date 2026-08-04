package org.craft.mcs.cap.core.account.user.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when an account lifecycle operation would alter or expose a retained deleted-user tombstone. */
public final class AccountUserLifecycleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AccountUserLifecycleException(@NonNull String message) {
    super(message);
  }
}
