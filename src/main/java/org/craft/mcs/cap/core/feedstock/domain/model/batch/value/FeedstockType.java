package org.craft.mcs.cap.core.feedstock.domain.model.batch.value;

import org.jspecify.annotations.NonNull;


public enum FeedstockType {

  APPLE_JUICE("Apple juice"),
  APPLE_MUST("Apple must"),
  PERRY_JUICE("Perry juice"),
  PERRY_MUST("Perry must"),
  BLEND("Blend"),
  UNKNOWN("Unknown");

  private final @NonNull String displayName;

  FeedstockType(@NonNull String displayName) {
    this.displayName = displayName;
  }

  public @NonNull String displayName() {
    return displayName;
  }
}
