package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateFeedstockMeasurementCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName name,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant measuredAt,
    @Nullable String note
) {

  public CreateFeedstockMeasurementCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock measurement name cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
    requireNonNull(measuredAt, "measured at cannot be null");

    note = normalize(note);
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
