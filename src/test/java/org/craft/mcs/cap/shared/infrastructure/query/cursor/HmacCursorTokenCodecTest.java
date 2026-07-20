package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class HmacCursorTokenCodecTest {

  private static final @NonNull Instant NOW = Instant.parse("2026-07-20T12:00:00Z");
  private static final byte @NonNull [] ACTIVE_KEY = key((byte) 1);
  private static final byte @NonNull [] PREVIOUS_KEY = key((byte) 2);
  private static final @NonNull CursorBinding BINDING = CursorBinding.boundTo(
      CursorScope.of("feedstock.measurements.measured-at-asc.v1"),
      "workspace=0195d5df-1111-7000-8000-000000000001",
      "season=2026"
  );

  @Test
  void roundTripsStronglyTypedPagePosition() {
    final var codec = codec(NOW, "active", Map.of("active", ACTIVE_KEY));
    final var key = new MeasurementPagePosition(
        Instant.parse("2026-07-19T10:15:30Z"),
        UUID.fromString("0195d5df-1111-7000-8000-000000000002")
    );

    final var token = codec.encode(BINDING, key);

    assertThat(token)
        .startsWith("v1.active.")
        .doesNotContain(key.measurementId().toString());
    assertThat(codec.decode(token, BINDING, MeasurementPagePosition.class))
        .isEqualTo(key);
  }

  @Test
  void rejectsTamperedToken() {
    final var codec = codec(NOW, "active", Map.of("active", ACTIVE_KEY));
    final var token = codec.encode(BINDING, keyAt(NOW));
    final var replacement = token.endsWith("A") ? "B" : "A";
    final var tampered = token.substring(0, token.length() - 1) + replacement;

    assertThatThrownBy(() -> codec.decode(tampered, BINDING, MeasurementPagePosition.class))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void rejectsReplayAgainstDifferentQueryBinding() {
    final var codec = codec(NOW, "active", Map.of("active", ACTIVE_KEY));
    final var token = codec.encode(BINDING, keyAt(NOW));
    final var differentBinding = CursorBinding.boundTo(
        CursorScope.of("feedstock.measurements.measured-at-desc.v1"),
        "workspace=0195d5df-1111-7000-8000-000000000001",
        "season=2026"
    );

    assertThatThrownBy(() -> codec.decode(token, differentBinding, MeasurementPagePosition.class))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void rejectsExpiredToken() {
    final var issuer = codec(NOW, "active", Map.of("active", ACTIVE_KEY));
    final var verifier = codec(NOW.plus(Duration.ofHours(2)), "active", Map.of("active", ACTIVE_KEY));
    final var token = issuer.encode(BINDING, keyAt(NOW));

    assertThatThrownBy(() -> verifier.decode(token, BINDING, MeasurementPagePosition.class))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void acceptsPreviousSigningKeyDuringRotation() {
    final var previousIssuer = codec(NOW, "previous", Map.of("previous", PREVIOUS_KEY));
    final var rotatedVerifier = codec(
        NOW,
        "active",
        Map.of("active", ACTIVE_KEY, "previous", PREVIOUS_KEY)
    );
    final var key = keyAt(NOW);
    final var token = previousIssuer.encode(BINDING, key);

    assertThat(rotatedVerifier.decode(token, BINDING, MeasurementPagePosition.class))
        .isEqualTo(key);
  }

  @Test
  void protectsSigningKeyFromCallerMutation() {
    final var mutableKey = key((byte) 3);
    final var codec = codec(NOW, "active", Map.of("active", mutableKey));
    final var token = codec.encode(BINDING, keyAt(NOW));

    Arrays.fill(mutableKey, (byte) 4);

    assertThat(codec.decode(token, BINDING, MeasurementPagePosition.class))
        .isEqualTo(keyAt(NOW));
  }

  @Test
  void rejectsShortSigningKeyWithConfigurationException() {
    final var shortKey = new byte[16];

    assertThatThrownBy(() -> codec(NOW, "active", Map.of("active", shortKey)))
        .isInstanceOf(CursorCodecConfigurationException.class)
        .hasMessage("cursor signing key must contain at least 32 bytes");
  }

  private static @NonNull HmacCursorTokenCodec codec(
      @NonNull Instant now,
      @NonNull String activeKeyId,
      @NonNull Map<@NonNull String, byte @NonNull []> keys
  ) {
    return new HmacCursorTokenCodec(
        JsonMapper.builder().findAndAddModules().build(),
        Clock.fixed(now, ZoneOffset.UTC),
        Duration.ofHours(1),
        activeKeyId,
        keys
    );
  }

  private static @NonNull MeasurementPagePosition keyAt(@NonNull Instant measuredAt) {
    return new MeasurementPagePosition(
        measuredAt,
        UUID.fromString("0195d5df-1111-7000-8000-000000000002")
    );
  }

  private static byte @NonNull [] key(byte value) {
    final var key = new byte[32];
    Arrays.fill(key, value);
    return key;
  }

  private record MeasurementPagePosition(
      @NonNull Instant measuredAt,
      @NonNull UUID measurementId
  ) {
  }
}
