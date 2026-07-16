package org.craft.mcs.cap.core.feedstock.domain.model.measurement;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@ValueObject
public record FeedstockMeasurementId(@NonNull UUID value) {

  public FeedstockMeasurementId {
    requireNonNull(value, "feedstock measurement id cannot be null");
  }

  public static @NonNull FeedstockMeasurementId newId() {
    return new FeedstockMeasurementId(newUuid());
  }

  public static @NonNull FeedstockMeasurementId of(@NonNull UUID value) {
    return new FeedstockMeasurementId(value);
  }

  public static @NonNull FeedstockMeasurementId of(@NonNull String value) {
    requireNonNull(value, "feedstock measurement id value cannot be null");
    return of(UUID.fromString(value));
  }
}
