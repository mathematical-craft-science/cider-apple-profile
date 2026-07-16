package org.craft.mcs.cap.shared.kernel;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Generic domain exception for violated invariants and unsupported domain rules.
 *
 * <p>Use a more specific {@link DomainException} subclass when the failure is
 * part of an explicit use case contract. This type is intended for value object
 * validation and internal domain strategy preconditions.
 */
public final class DomainRuleViolationException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DomainRuleViolationException(@NonNull String message) {
    super(message);
  }
}
