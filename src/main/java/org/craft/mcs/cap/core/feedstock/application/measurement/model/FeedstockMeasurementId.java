package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/**
 * Compatibility identity exposed by legacy Feedstock measurement scenarios.
 *
 * <p>The value identifies a generic Measurement Set; Feedstock no longer owns a measurement aggregate.
 */
public record FeedstockMeasurementId(@NonNull UUID value) {

  public FeedstockMeasurementId {
    requireNonNull(value, "feedstock measurement id cannot be null");
  }

  public static @NonNull FeedstockMeasurementId newId() {
    return of(newUuid());
  }

  public static @NonNull FeedstockMeasurementId of(@NonNull UUID value) {
    return new FeedstockMeasurementId(value);
  }

  public static @NonNull FeedstockMeasurementId of(@NonNull String value) {
    requireNonNull(value, "feedstock measurement id value cannot be null");
    return of(UUID.fromString(value));
  }
}
