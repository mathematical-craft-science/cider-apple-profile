package org.craft.mcs.cap.shared.platform.query;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;


public final class OpaqueCursorCodec {

  private static final Base64.@NonNull Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.@NonNull Decoder DECODER = Base64.getUrlDecoder();

  private OpaqueCursorCodec() {
  }

  public static @NonNull String encode(@NonNull String @NonNull ... parts) {
    requireNonNull(parts, "cursor parts cannot be null");

    if (parts.length == 0) {
      throw new IllegalArgumentException("cursor must contain at least one part");
    }

    return Arrays.stream(parts)
        .map(part -> ENCODER.encodeToString(requireNonNull(part, "cursor part cannot be null")
            .getBytes(StandardCharsets.UTF_8)))
        .collect(joining("."));
  }

  public static @NonNull List<@NonNull String> decode(
      @NonNull String cursor,
      int expectedParts
  ) {
    requireNonNull(cursor, "cursor cannot be null");

    if (expectedParts < 1) {
      throw new IllegalArgumentException("expected cursor parts must be positive");
    }

    final var parts = cursor.split("\\.", -1);
    if (parts.length != expectedParts) {
      throw new MalformedCursorException();
    }

    try {
      return Arrays.stream(parts)
          .map(part -> new String(DECODER.decode(part), StandardCharsets.UTF_8))
          .toList();
    } catch (IllegalArgumentException exception) {
      throw new MalformedCursorException(exception);
    }
  }
}
