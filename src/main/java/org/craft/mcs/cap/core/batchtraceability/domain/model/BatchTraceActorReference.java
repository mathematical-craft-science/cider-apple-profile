package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Provider-neutral identity of the actor responsible for a traceability step. */
@ValueObject
public record BatchTraceActorReference(
    @NonNull BatchTraceActorType type,
    @NonNull String id
) {

  public BatchTraceActorReference {
    requireNonNull(type, "batch trace actor type cannot be null");
    id = requireNonNull(id, "batch trace actor id cannot be null").strip();
    if (id.isEmpty()) {
      throw new DomainRuleViolationException("batch trace actor id cannot be blank");
    }
  }

  /** Creates a reference to a CAP-owned account identity. */
  public static @NonNull BatchTraceActorReference accountUser(@NonNull String accountUserId) {
    return new BatchTraceActorReference(BatchTraceActorType.ACCOUNT_USER, accountUserId);
  }

  /** Creates a reference to a named internal system process. */
  public static @NonNull BatchTraceActorReference system(@NonNull String processId) {
    return new BatchTraceActorReference(BatchTraceActorType.SYSTEM, processId);
  }
}
