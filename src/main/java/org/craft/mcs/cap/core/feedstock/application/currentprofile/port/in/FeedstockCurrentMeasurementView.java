package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Latest measurement evidence used to derive the current feedstock chemical profile.
 */
public record FeedstockCurrentMeasurementView(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull FeedstockMeasurementName name,
    @NonNull Instant measuredAt,
    @NonNull ChemicalProfile chemicalProfile,
    @Nullable String note
) {

  public FeedstockCurrentMeasurementView {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(name, "feedstock measurement name cannot be null");
    requireNonNull(measuredAt, "measured at cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }
}
