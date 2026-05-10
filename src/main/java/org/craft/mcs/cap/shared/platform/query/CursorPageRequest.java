package org.craft.mcs.cap.shared.platform.query;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record CursorPageRequest(
    @Nullable String after,
    int limit
) {

  public static final int DEFAULT_LIMIT = 50;
  public static final int MAX_LIMIT = 200;

  public CursorPageRequest {
    after = normalize(after);

    if (limit < 1 || limit > MAX_LIMIT) {
      throw new IllegalArgumentException("cursor page limit must be between 1 and " + MAX_LIMIT);
    }
  }

  public static @NonNull CursorPageRequest firstPage() {
    return new CursorPageRequest(null, DEFAULT_LIMIT);
  }

  public static @NonNull CursorPageRequest firstPage(int limit) {
    return new CursorPageRequest(null, limit);
  }

  private static @Nullable String normalize(@Nullable String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    return cursor.strip();
  }
}
