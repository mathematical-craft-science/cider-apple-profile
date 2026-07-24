package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class FeedstockMeasurementNotFoundException
    extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockMeasurementId measurementId;

  public FeedstockMeasurementNotFoundException(@NonNull FeedstockMeasurementId measurementId) {
    super("feedstock measurement not found: "
        + requireNonNull(measurementId, "feedstock measurement id cannot be null").value());
    this.measurementId = measurementId;
  }

  public @NonNull FeedstockMeasurementId measurementId() {
    return measurementId;
  }
}
