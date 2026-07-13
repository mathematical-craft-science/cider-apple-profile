package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.time.Duration;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Effective workspace-owned settings required by Feedstock current-profile assessments. */
public record FeedstockCurrentProfileSettings(
    @NonNull Duration measurementStaleAfter
) {

  public FeedstockCurrentProfileSettings {
    requireNonNull(measurementStaleAfter, "measurement stale duration cannot be null");
  }
}
