package org.craft.mcs.cap.core.feedstock.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record FeedstockSampleId(@NonNull UUID value) {

  public FeedstockSampleId {
    requireNonNull(value, "feedstock sample id cannot be null");
  }

  public static @NonNull FeedstockSampleId newId() {
    return new FeedstockSampleId(UUID.randomUUID());
  }

  public static @NonNull FeedstockSampleId of(@NonNull UUID value) {
    return new FeedstockSampleId(value);
  }

  public static @NonNull FeedstockSampleId of(@NonNull String value) {
    requireNonNull(value, "feedstock sample id value cannot be null");
    return of(UUID.fromString(value));
  }
}
