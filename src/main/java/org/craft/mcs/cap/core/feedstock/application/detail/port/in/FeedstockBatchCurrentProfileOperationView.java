package org.craft.mcs.cap.core.feedstock.application.detail.port.in;

import java.time.Instant;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compact last-operation marker for the current profile card.
 */
public record FeedstockBatchCurrentProfileOperationView(
    @NonNull FeedstockBatchCurrentProfileOperationType type,
    @NonNull Instant occurredAt
) {

  public FeedstockBatchCurrentProfileOperationView {
    requireNonNull(type, "feedstock current profile operation type cannot be null");
    requireNonNull(occurredAt, "feedstock current profile operation time cannot be null");
  }
}
