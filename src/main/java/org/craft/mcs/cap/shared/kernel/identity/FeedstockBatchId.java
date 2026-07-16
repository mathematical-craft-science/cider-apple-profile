package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@ValueObject
public record FeedstockBatchId(@NonNull UUID value) {

  public FeedstockBatchId {
    requireNonNull(value, "feedstock batch id cannot be null");
  }

  public static @NonNull FeedstockBatchId newId() {
    return new FeedstockBatchId(newUuid());
  }

  public static @NonNull FeedstockBatchId of(@NonNull UUID value) {
    return new FeedstockBatchId(value);
  }

  public static @NonNull FeedstockBatchId of(@NonNull String value) {
    requireNonNull(value, "feedstock batch id value cannot be null");
    return of(UUID.fromString(value));
  }
}
