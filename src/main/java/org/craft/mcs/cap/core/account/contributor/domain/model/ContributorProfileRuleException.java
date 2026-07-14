package org.craft.mcs.cap.core.account.contributor.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when contributor contact visibility conflicts with explicit publication consent. */
public final class ContributorProfileRuleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ContributorProfileRuleException(@NonNull String message) {
    super(message);
  }
}
