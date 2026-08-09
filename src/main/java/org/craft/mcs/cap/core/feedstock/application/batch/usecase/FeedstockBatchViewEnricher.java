package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.application.vessel.port.out.FeedstockVesselReferenceQuery;
import org.craft.mcs.cap.shared.application.material.VesselReferenceNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Enriches feedstock batch state with current Vessel catalog display data in one bulk lookup. */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchViewEnricher {

  private final @NonNull FeedstockVesselReferenceQuery vesselReferenceQuery;

  public @NonNull FeedstockBatchView enrich(@NonNull FeedstockBatchSummary batch) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    return enrichAll(batch.workspaceId(), List.of(batch)).getFirst();
  }

  public @NonNull List<@NonNull FeedstockBatchView> enrichAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull List<@NonNull FeedstockBatchSummary> batches
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var sourceBatches = List.copyOf(requireNonNull(batches, "feedstock batches cannot be null"));
    final Set<VesselId> vesselIds = sourceBatches.stream()
        .map(FeedstockBatchSummary::vesselId)
        .collect(Collectors.toUnmodifiableSet());
    final var vessels = vesselReferenceQuery.resolveReferences(workspaceId, vesselIds);
    return sourceBatches.stream()
        .map(batch -> new FeedstockBatchView(
            batch,
            Optional.ofNullable(vessels.get(batch.vesselId()))
                .orElseThrow(() -> new VesselReferenceNotFoundException(batch.vesselId()))
        ))
        .toList();
  }
}
