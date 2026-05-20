package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Production trace source for material consumed directly from a feedstock batch.
 */
public record ProductionBatchFeedstockTraceSource(
    @NonNull FeedstockBatchId feedstockBatchId,
    @Nullable String displayName,
    @NonNull VolumeValue volume
) implements ProductionBatchTraceSource {

  public ProductionBatchFeedstockTraceSource {
    requireNonNull(feedstockBatchId, "feedstock batch id cannot be null");
    displayName = normalize(displayName);
    requireNonNull(volume, "production trace source volume cannot be null");

    if (volume.isZero()) {
      throw new ApplicationValidationException("production trace source volume must be positive");
    }
  }

  @Override
  public @NonNull String id() {
    return feedstockBatchId.value().toString();
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
