package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * References one immutable material state participating in a trace step.
 *
 * <p>{@link #traceStateId()} identifies the historical state, while {@link #subject()} identifies
 * the stable business material. An output snapshot defines the produced state. An input snapshot
 * describes the portion and measured profile participating in that particular step; it does not
 * redefine the referenced state.</p>
 */
@ValueObject
public record BatchTraceStepSubject(
    @NonNull MaterialStateId traceStateId,
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot
) {

  public BatchTraceStepSubject {
    requireNonNull(traceStateId, "batch trace state id cannot be null");
    requireNonNull(subject, "batch trace step subject cannot be null");
  }

  public static @NonNull BatchTraceStepSubject of(@NonNull BatchTraceSubject subject) {
    return new BatchTraceStepSubject(MaterialStateId.newId(), subject, null);
  }

  /** Captures a new immutable material state for a business subject. */
  public static @NonNull BatchTraceStepSubject capture(
      @NonNull BatchTraceSubject subject,
      @NonNull BatchTraceMaterialSnapshot snapshot
  ) {
    return new BatchTraceStepSubject(
        MaterialStateId.newId(),
        subject,
        snapshot
    );
  }
}
