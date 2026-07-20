package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Binds an opaque cursor token to one query scope and an immutable canonical filter set. */
public record CursorBinding(
    @NonNull CursorScope scope,
    @NonNull List<@NonNull String> components
) {

  public CursorBinding {
    requireNonNull(scope, "cursor scope cannot be null");
    components = List.copyOf(requireNonNull(components, "cursor binding components cannot be null"));
  }

  public static @NonNull CursorBinding boundTo(
      @NonNull CursorScope scope,
      @NonNull String @NonNull ... components
  ) {
    requireNonNull(components, "cursor binding components cannot be null");
    return new CursorBinding(scope, List.of(components));
  }
}
