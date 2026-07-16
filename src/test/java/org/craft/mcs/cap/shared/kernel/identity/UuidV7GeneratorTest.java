package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class UuidV7GeneratorTest {

  private static final long TIMESTAMP_MASK = 0x0000_FFFF_FFFF_FFFFL;

  @Test
  void generatesRfc9562Version7IdentifierWithCurrentTimestamp() {
    final var before = System.currentTimeMillis();
    final var uuid = newUuid();
    final var after = System.currentTimeMillis();

    assertThat(uuid)
        .returns(7, UUID::version)
        .returns(2, UUID::variant)
        .satisfies(generated -> assertThat(timestampOf(generated)).isBetween(before, after));
  }

  @Test
  void generatesUniqueMonotonicallyOrderedIdentifiers() {
    final var generated = IntStream.range(0, 10_000)
        .mapToObj(ignored -> newUuid())
        .toList();

    assertThat(generated)
        .doesNotHaveDuplicates()
        .isSorted();
  }

  private static long timestampOf(@NonNull UUID uuid) {
    return (uuid.getMostSignificantBits() >>> 16) & TIMESTAMP_MASK;
  }
}
