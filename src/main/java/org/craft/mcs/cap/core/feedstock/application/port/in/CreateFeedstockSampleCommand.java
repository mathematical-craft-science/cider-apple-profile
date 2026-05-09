package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateFeedstockSampleCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockSampleName name,
    @NonNull FeedstockType type,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant measuredAt,
    @Nullable String note
) {

  public CreateFeedstockSampleCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock sample name cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
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
