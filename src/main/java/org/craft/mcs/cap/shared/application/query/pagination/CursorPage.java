package org.craft.mcs.cap.shared.application.query.pagination;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Application query result with a typed continuation position for the next page. */
public record CursorPage<
    T extends @NonNull Object,
    C extends @NonNull Object
>(
    @NonNull List<T> items,
    @Nullable C nextPosition
) {

  public CursorPage {
    requireNonNull(items, "cursor page items cannot be null");
    items = List.copyOf(items);
  }
}
