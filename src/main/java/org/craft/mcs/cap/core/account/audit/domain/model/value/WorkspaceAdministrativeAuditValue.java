package org.craft.mcs.cap.core.account.audit.domain.model.value;

import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditRuleException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Preserves one bounded before-or-after value in an immutable workspace administration fact. */
@ValueObject
public record WorkspaceAdministrativeAuditValue(@NonNull String value) {

  private static final int MAX_LENGTH = 2_000;

  public WorkspaceAdministrativeAuditValue {
    requireNonNull(value, "workspace administrative audit value cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new WorkspaceAdministrativeAuditRuleException(
          "workspace administrative audit value cannot be blank"
      );
    }
    if (value.length() > MAX_LENGTH) {
      throw new WorkspaceAdministrativeAuditRuleException(
          "workspace administrative audit value cannot exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  /** Creates a validated immutable representation of an audited value. */
  public static @NonNull WorkspaceAdministrativeAuditValue of(@NonNull String value) {
    return new WorkspaceAdministrativeAuditValue(value);
  }
}
