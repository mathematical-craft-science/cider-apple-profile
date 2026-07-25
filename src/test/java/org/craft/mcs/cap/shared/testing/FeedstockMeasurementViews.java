package org.craft.mcs.cap.shared.testing;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

/**
 * Adapts immutable fixtures to measurement views in read-side tests.
 */
public final class FeedstockMeasurementViews {

  private FeedstockMeasurementViews() {
  }

  public static @NonNull FeedstockMeasurementView view(
      @NonNull FeedstockMeasurementFixture measurement
  ) {
    return new FeedstockMeasurementView(
        measurement.measurementId(),
        measurement.batchId(),
        MaterialStateId.newId(),
        measurement.name(),
        measurement.chemicalProfile(),
        measurement.measuredAt(),
        0,
        measurement.note()
    );
  }
}
