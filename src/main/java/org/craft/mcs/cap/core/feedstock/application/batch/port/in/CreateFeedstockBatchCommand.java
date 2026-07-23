package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateFeedstockBatchCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @Nullable Vessel vessel,
    @NonNull FeedstockType type,
    @NonNull FeedstockSeason season,
    @Nullable FeedstockSource source,
    @NonNull VolumeValue availableVolume,
    @Nullable String note
) {

  public CreateFeedstockBatchCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
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
