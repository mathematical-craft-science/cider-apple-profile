package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Feedstock-local metadata projection of the latest MeasurementSet contributing evidence.
 *
 * <p>This value identifies a navigation and ordering anchor. It does not claim that every current
 * characteristic originated from the same MeasurementSet and does not redefine Measurement domain
 * ownership.
 */
public record FeedstockLatestMeasurementSetView(
    @NonNull UUID measurementSetId,
    @NonNull String name,
    @NonNull Instant measuredAt,
    long version,
    @Nullable String note
) {

  private static final int MAX_NAME_LENGTH = 160;

  public FeedstockLatestMeasurementSetView {
    requireNonNull(measurementSetId, "latest feedstock measurement set id cannot be null");
    requireNonNull(name, "latest feedstock measurement name cannot be null");
    requireNonNull(measuredAt, "latest feedstock measurement time cannot be null");
    name = name.strip();
    if (name.isEmpty()) {
      throw new ApplicationValidationException("latest feedstock measurement name cannot be blank");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new ApplicationValidationException(
          "latest feedstock measurement name length must not exceed " + MAX_NAME_LENGTH + " characters"
      );
    }
    if (version < 0) {
      throw new ApplicationValidationException("latest feedstock measurement version cannot be negative");
    }
  }
}
