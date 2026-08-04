package org.craft.mcs.cap.core.feedstock.application.currentprofile.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Query boundary for state-aware current profiles of Feedstock batches. */
@Component
@RequiredArgsConstructor
public final class FeedstockCurrentProfileQueryService {

  private final @NonNull FeedstockCurrentProfileQueryRepository repository;

  public @NonNull FeedstockCurrentProfileView requireCurrentProfile(
      @NonNull FeedstockBatchId batchId
  ) {
    return findCurrentProfile(requireNonNull(batchId, "feedstock batch id cannot be null"))
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batchId));
  }

  public @NonNull Optional<@NonNull FeedstockCurrentProfileView> findCurrentProfile(
      @NonNull FeedstockBatchId batchId
  ) {
    return repository.findByBatchId(
        requireNonNull(batchId, "feedstock batch id cannot be null")
    );
  }

  /**
   * Returns the profile only when it describes the same material state as the batch snapshot.
   */
  public @NonNull Optional<@NonNull FeedstockCurrentProfileView> findCurrentProfile(
      @NonNull FeedstockBatchSummary batch
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    return findCurrentProfile(batch.batchId())
        .filter(profile -> profile.currentMaterialStateId().equals(batch.currentMaterialStateId()));
  }

  public @NonNull List<@NonNull FeedstockCurrentProfileView> findCurrentProfilesByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return batchIds.isEmpty() ? List.of() : repository.findByBatchIds(batchIds);
  }

  /**
   * Returns profiles coherent with the supplied batch snapshots and omits concurrently replaced states.
   */
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findCurrentProfiles(
      @NonNull List<@NonNull FeedstockBatchSummary> batches
  ) {
    requireNonNull(batches, "feedstock batch summaries cannot be null");
    final Map<FeedstockBatchId, FeedstockBatchSummary> batchesById = batches.stream()
        .collect(Collectors.toUnmodifiableMap(FeedstockBatchSummary::batchId, Function.identity()));
    return findCurrentProfilesByBatchIds(batchesById.keySet())
        .stream()
        .filter(profile -> hasSameMaterialState(batchesById, profile))
        .toList();
  }

  private static boolean hasSameMaterialState(
      @NonNull Map<FeedstockBatchId, FeedstockBatchSummary> batchesById,
      @NonNull FeedstockCurrentProfileView profile
  ) {
    final var batch = batchesById.get(profile.batchId());
    return batch != null
        && batch.currentMaterialStateId().equals(profile.currentMaterialStateId());
  }
}
