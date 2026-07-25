package org.craft.mcs.cap.shared.testing;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Deterministic UUIDv7-shaped identities for readable batch-trace test fixtures. */
public final class BatchTraceSubjects {

  private static final long VERSION_7_MASK = 0x0000_0000_0000_7000L;
  private static final long VERSION_CLEAR_MASK = 0xFFFF_FFFF_FFFF_0FFFL;
  private static final long RFC_4122_VARIANT_MASK = 0x8000_0000_0000_0000L;
  private static final long VARIANT_CLEAR_MASK = 0x3FFF_FFFF_FFFF_FFFFL;

  private BatchTraceSubjects() {
  }

  public static @NonNull BatchTraceSubject subject(
      @NonNull BatchTraceSubjectType type,
      @NonNull String fixtureReference
  ) {
    return BatchTraceSubject.of(type, id(fixtureReference));
  }

  public static @NonNull UUID id(@NonNull String fixtureReference) {
    final var source = UUID.nameUUIDFromBytes(
        requireNonNull(fixtureReference, "fixture reference cannot be null")
            .getBytes(StandardCharsets.UTF_8)
    );
    return new UUID(
        source.getMostSignificantBits() & VERSION_CLEAR_MASK | VERSION_7_MASK,
        source.getLeastSignificantBits() & VARIANT_CLEAR_MASK | RFC_4122_VARIANT_MASK
    );
  }
}
