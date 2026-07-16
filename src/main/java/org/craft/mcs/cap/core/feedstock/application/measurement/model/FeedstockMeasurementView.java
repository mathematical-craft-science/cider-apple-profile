package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable application projection of measurement evidence used by feedstock read scenarios. */
public record FeedstockMeasurementView(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName name,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant measuredAt,
    long evidenceVersion,
    @Nullable String note
) {

  public FeedstockMeasurementView {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock measurement name cannot be null");
    requireNonNull(chemicalProfile, "feedstock chemical profile cannot be null");
    requireNonNull(measuredAt, "feedstock measurement time cannot be null");
    if (evidenceVersion < 0) {
      throw new ApplicationValidationException("feedstock measurement evidence version cannot be negative");
    }
  }
}
