package org.craft.mcs.cap.core.feedstock.application.detail.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchViewEnricher;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchDetailView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Builds feedstock batch details from batch metadata and the state-aware current-profile projection.
 *
 * <p>Measurement history remains owned by the generic Measurement context. This service exposes
 * only the independently selected evidence needed to explain the current feedstock profile.
 */
@Component
@RequiredArgsConstructor
public final class GetFeedstockBatchDetailService implements GetFeedstockBatchDetailUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull FeedstockCurrentProfileAssessmentService currentProfileAssessmentService;
  private final @NonNull FeedstockBatchViewEnricher batchViewEnricher;

  @Override
  public @NonNull FeedstockBatchDetailView get(@NonNull GetFeedstockBatchDetailQuery query) {
    requireNonNull(query, "feedstock batch detail query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());

    final var batch = feedstockBatchRepository.findById(query.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(query.batchId()));
    if (!batch.workspaceId().equals(query.workspaceId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          query.workspaceId(),
          batch.workspaceId()
      );
    }

    final var batchSummary = FeedstockBatchSummaryMapper.toSummary(batch);
    final var currentProfile = currentProfileQueryService.findCurrentProfile(batchSummary)
        .orElse(null); // NOSONAR

    return new FeedstockBatchDetailView(
        batchViewEnricher.enrich(batchSummary),
        toCurrentProfile(batchSummary, currentProfile)
    );
  }

  private @NonNull FeedstockBatchCurrentProfileView toCurrentProfile(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    if (currentProfile == null) {
      return new FeedstockBatchCurrentProfileView(
          null,
          currentProfileAssessmentService.assess(batch, null)
      );
    }

    return new FeedstockBatchCurrentProfileView(
        currentProfile.summary(),
        currentProfileAssessmentService.assess(batch, currentProfile)
    );
  }

}
