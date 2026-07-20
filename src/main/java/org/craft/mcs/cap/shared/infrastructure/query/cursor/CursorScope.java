package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Stable endpoint and sort contract to which an opaque cursor token is bound. */
public record CursorScope(@NonNull String value) {

  private static final int MAX_LENGTH = 120;
  private static final @NonNull Pattern VALID_VALUE = Pattern.compile("[a-z0-9][a-z0-9._-]*");

  public CursorScope {
    requireNonNull(value, "cursor scope cannot be null");
    value = value.strip();

    if (value.isEmpty() || value.length() > MAX_LENGTH || !VALID_VALUE.matcher(value).matches()) {
      throw new CursorDefinitionException("cursor scope is invalid");
    }
  }

  public static @NonNull CursorScope of(@NonNull String value) {
    return new CursorScope(value);
  }
}
