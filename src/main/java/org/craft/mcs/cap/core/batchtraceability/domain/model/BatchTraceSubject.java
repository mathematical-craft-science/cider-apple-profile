package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceSubject(
    @NonNull BatchTraceSubjectType type,
    @NonNull String id,
    @Nullable String displayNameSnapshot
) {

  public BatchTraceSubject {
    requireNonNull(type, "batch trace subject type cannot be null");
    id = normalizeRequired(id, "batch trace subject id cannot be blank");
    displayNameSnapshot = normalize(displayNameSnapshot);
  }

  public static @NonNull BatchTraceSubject of(
      @NonNull BatchTraceSubjectType type,
      @NonNull String id
  ) {
    return new BatchTraceSubject(type, id, null);
  }

  private static @NonNull String normalizeRequired(@NonNull String value, @NonNull String message) {
    final var normalized = requireNonNull(value, message).strip();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return normalized;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }
    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
