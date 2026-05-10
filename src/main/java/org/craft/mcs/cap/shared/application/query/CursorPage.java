package org.craft.mcs.cap.shared.application.query;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CursorPage<T extends @NonNull Object>(
    @NonNull List<T> items,
    @Nullable String nextCursor
) {

  public CursorPage {
    requireNonNull(items, "cursor page items cannot be null");
    items = List.copyOf(items);
  }
}
