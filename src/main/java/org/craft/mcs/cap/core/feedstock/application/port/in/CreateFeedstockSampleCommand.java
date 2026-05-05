package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateFeedstockSampleCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockSampleName name,
    @NonNull FeedstockType type,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant collectedAt,
    @Nullable String note
) {

  public CreateFeedstockSampleCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "feedstock sample name cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
    requireNonNull(collectedAt, "collected at cannot be null");

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
