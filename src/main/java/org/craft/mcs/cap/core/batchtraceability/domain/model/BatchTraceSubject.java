package org.craft.mcs.cap.core.batchtraceability.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Stable business subject identity used across immutable material states.
 *
 * <p>{@code displayNameSnapshot} is intentionally a string: it preserves a historical label and is
 * not the authoritative name of the owning aggregate.
 */
@ValueObject
public record BatchTraceSubject(
    @NonNull BatchTraceSubjectType type,
    @NonNull UUID id,
    @Nullable String displayNameSnapshot
) {

  public BatchTraceSubject {
    requireNonNull(type, "batch trace subject type cannot be null");
    requireNonNull(id, "batch trace subject id cannot be null");
    displayNameSnapshot = normalize(displayNameSnapshot);
  }

  public static @NonNull BatchTraceSubject of(
      @NonNull BatchTraceSubjectType type,
      @NonNull UUID id
  ) {
    return new BatchTraceSubject(type, id, null);
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }
    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
