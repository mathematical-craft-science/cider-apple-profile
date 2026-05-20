package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceStepSubject(
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot
) {

  public BatchTraceStepSubject {
    requireNonNull(subject, "batch trace step subject cannot be null");
  }

  public static @NonNull BatchTraceStepSubject of(@NonNull BatchTraceSubject subject) {
    return new BatchTraceStepSubject(subject, null);
  }
}
