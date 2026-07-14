package org.craft.mcs.cap.core.account.domain.model;

import java.time.Duration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Product settings shared by every member of a workspace. */
@AggregateRoot
public record WorkspaceSettings(
    @NonNull WorkspaceId workspaceId,
    @NonNull Duration measurementStaleAfter,
    @NonNull TitratableAcidityBasis defaultAcidityBasis
) {

  public WorkspaceSettings {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(measurementStaleAfter, "measurement stale duration cannot be null");
    if (measurementStaleAfter.isZero() || measurementStaleAfter.isNegative()) {
      throw new DomainRuleViolationException("measurement stale duration must be positive");
    }
    requireNonNull(defaultAcidityBasis, "default acidity basis cannot be null");
  }

}
