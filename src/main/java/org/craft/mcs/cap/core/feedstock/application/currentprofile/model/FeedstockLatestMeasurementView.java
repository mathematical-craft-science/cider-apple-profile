package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Metadata of the latest measurement contributing evidence to a composite current profile.
 *
 * <p>This value identifies a navigation and ordering anchor. It does not claim that every current
 * characteristic originated from the same measurement set.
 */
public record FeedstockLatestMeasurementView(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull FeedstockMeasurementName name,
    @NonNull Instant measuredAt,
    long version,
    @Nullable String note
) {

  public FeedstockLatestMeasurementView {
    requireNonNull(measurementId, "latest feedstock measurement id cannot be null");
    requireNonNull(name, "latest feedstock measurement name cannot be null");
    requireNonNull(measuredAt, "latest feedstock measurement time cannot be null");
    if (version < 0) {
      throw new ApplicationValidationException("latest feedstock measurement version cannot be negative");
    }
  }
}
