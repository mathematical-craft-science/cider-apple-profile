package org.craft.mcs.cap.shared.testing;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.jspecify.annotations.NonNull;

/**
 * Adapts aggregate fixtures to immutable measurement views in read-side tests.
 */
public final class FeedstockMeasurementViews {

  private FeedstockMeasurementViews() {
  }

  public static @NonNull FeedstockMeasurementView view(
      @NonNull FeedstockMeasurement measurement
  ) {
    return new FeedstockMeasurementView(
        measurement.measurementId(),
        measurement.batchId(),
        measurement.name(),
        measurement.chemicalProfile(),
        measurement.measuredAt(),
        0,
        measurement.note()
    );
  }
}
