package org.craft.mcs.cap.shared.application.query.pagination;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Application query page request containing a typed keyset position, never an opaque transport token. */
public record CursorPageRequest<C extends @NonNull Object>(
    @Nullable C after,
    int limit
) {

  public CursorPageRequest {
    if (limit < 1 || limit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "cursor page limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }

  public static <C extends @NonNull Object> @NonNull CursorPageRequest<C> firstPage() {
    return new CursorPageRequest<>(null, QueryLimits.DEFAULT);
  }

  public static <C extends @NonNull Object> @NonNull CursorPageRequest<C> firstPage(int limit) {
    return new CursorPageRequest<>(null, limit);
  }
}
