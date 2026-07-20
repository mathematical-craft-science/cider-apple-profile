package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * References one immutable material state participating in a trace operation.
 *
 * <p>{@link #traceStateId()} identifies the historical state, while {@link #subject()} identifies
 * the stable business material whose state was captured.
 */
@ValueObject
public record BatchTraceStepSubject(
    @NonNull BatchTraceStateId traceStateId,
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot
) {

  public BatchTraceStepSubject {
    requireNonNull(traceStateId, "batch trace state id cannot be null");
    requireNonNull(subject, "batch trace step subject cannot be null");
  }

  public static @NonNull BatchTraceStepSubject of(@NonNull BatchTraceSubject subject) {
    return new BatchTraceStepSubject(BatchTraceStateId.newId(), subject, null);
  }

  /** Captures a new immutable material state for a business subject. */
  public static @NonNull BatchTraceStepSubject capture(
      @NonNull BatchTraceSubject subject,
      @NonNull BatchTraceMaterialSnapshot snapshot
  ) {
    return new BatchTraceStepSubject(
        BatchTraceStateId.newId(),
        subject,
        snapshot
    );
  }
}
