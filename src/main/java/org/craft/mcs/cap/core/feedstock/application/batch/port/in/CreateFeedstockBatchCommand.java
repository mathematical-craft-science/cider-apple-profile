package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateFeedstockBatchCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchName name,
    @NonNull FeedstockType type,
    @NonNull FeedstockSeason season,
    @Nullable FeedstockSource source,
    @NonNull VolumeValue availableVolume,
    @Nullable String note
) {

  public CreateFeedstockBatchCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "feedstock batch name cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
    requireNonNull(season, "feedstock season cannot be null");
    requireNonNull(availableVolume, "available volume cannot be null");

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
