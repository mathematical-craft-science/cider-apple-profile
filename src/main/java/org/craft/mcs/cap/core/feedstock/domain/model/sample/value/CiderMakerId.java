package org.craft.mcs.cap.core.feedstock.domain.model.sample.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record CiderMakerId(@NonNull UUID value) {

  public CiderMakerId {
    requireNonNull(value, "cider maker id cannot be null");
  }

  public static @NonNull CiderMakerId of(@NonNull UUID value) {
    return new CiderMakerId(value);
  }

  public static @NonNull CiderMakerId of(@NonNull String value) {
    requireNonNull(value, "cider maker id value cannot be null");
    return of(UUID.fromString(value));
  }
}
