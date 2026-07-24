package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataRetrievalFailureException;

import static java.util.Objects.requireNonNull;


/** Encodes a typed CAP actor reference into the compact representation used by measurement history. */
final class ActorReferenceJdbcCodec {

  private static final String SEPARATOR = ":";

  private ActorReferenceJdbcCodec() {
  }

  static @NonNull String encode(@NonNull ActorReference actor) {
    requireNonNull(actor, "actor reference cannot be null");
    return actor.type().name() + SEPARATOR + actor.id();
  }

  static @NonNull ActorReference decode(@NonNull String value) {
    requireNonNull(value, "persisted actor reference cannot be null");
    final var separatorIndex = value.indexOf(SEPARATOR);
    if (separatorIndex <= 0 || separatorIndex == value.length() - 1) {
      throw invalidReference(value, null);
    }
    try {
      return new ActorReference(
          ActorType.valueOf(value.substring(0, separatorIndex)),
          UUID.fromString(value.substring(separatorIndex + 1))
      );
    } catch (IllegalArgumentException exception) {
      throw invalidReference(value, exception);
    }
  }

  private static @NonNull DataRetrievalFailureException invalidReference(
      @NonNull String value,
      @Nullable Exception cause
  ) {
    return new DataRetrievalFailureException("invalid persisted measurement actor reference: " + value, cause);
  }
}
