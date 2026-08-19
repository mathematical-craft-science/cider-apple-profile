package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.usecase;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningBatchSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.out.FeedstockPlanningBatchSnapshotProvider;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Application read model for the feedstock planning.
 *
 * <p>This query service deliberately composes batch metadata, current material-state evidence, graph
 * projection, target candidate scoring, ABV, YAN and fermentation-start readiness into one application read model. It
 * is not a domain aggregate; its responsibility is to give API clients a coherent planning snapshot without forcing
 * them to stitch several business endpoints together.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockPlanningBatchesService
    implements QueryFeedstockPlanningBatchesUseCase {

  private final @NonNull FeedstockPlanningBatchSnapshotProvider batchSnapshotProvider;
  private final @NonNull QueryFeedstockBatchProfileTargetCandidatesUseCase queryTargetCandidatesUseCase;
  private final @NonNull FeedstockPlanningAccessControl accessControl;

  @Override
  public @NonNull FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query) {
    requireNonNull(query, "feedstock planning batch view query cannot be null");
    accessControl.requireReadAccess(query.workspaceId());
    final var batches = batchSnapshotProvider.findBatches(query.workspaceId(), query.season());
    final var targetCandidates = targetCandidates(query);
    return new FeedstockPlanningView(
        batches.stream()
            .map(batch -> toBatchView(
                batch,
                targetCandidates.candidatesByBatchId().get(batch.batch().batch().batchId())
            ))
            .toList(),
        targetCandidates.analysis()
    );
  }

  private @NonNull FeedstockPlanningBatchView toBatchView(
      @NonNull FeedstockPlanningBatchSnapshot batch,
      @Nullable FeedstockPlanningTargetCandidateView targetCandidate
  ) {
    final var currentProfile = batch.currentProfile();
    final var coherentTargetCandidate = targetCandidateFor(currentProfile, targetCandidate);
    return new FeedstockPlanningBatchView(
        batch.batch(),
        currentProfile == null ? null : currentProfile.summary(),
        batch.assessment(),
        coherentTargetCandidate
    );
  }

  private static @Nullable FeedstockPlanningTargetCandidateView targetCandidateFor(
      @Nullable FeedstockCurrentProfileView currentProfile,
      @Nullable FeedstockPlanningTargetCandidateView targetCandidate
  ) {
    if (currentProfile == null || targetCandidate == null) {
      return null;
    }
    return currentProfile.currentMaterialStateId().equals(targetCandidate.currentMaterialStateId())
           ? targetCandidate
           : null;
  }

  private @NonNull TargetCandidates targetCandidates(@NonNull FeedstockPlanningBatchViewQuery query) {
    final var target = query.target();
    if (target == null) {
      return TargetCandidates.empty();
    }

    final var candidates = queryTargetCandidatesUseCase.query(new FeedstockBatchProfileTargetQuery(
        query.workspaceId(),
        new FeedstockPlanningProfileFilters(null, query.season()),
        null,
        query.analysisScenario(),
        target,
        query.tanninTarget(),
        query.weights(),
        query.targetCandidateLimit()
    ));

    return new TargetCandidates(
        new FeedstockPlanningTargetAnalysisView(candidates.analysisScenario(), candidates.normalizerCode()),
        candidates.candidates().stream()
            .collect(Collectors.toMap(
                FeedstockBatchProfileTargetCandidate::batchId,
                this::toTargetCandidateView
            ))
    );
  }

  private @NonNull FeedstockPlanningTargetCandidateView toTargetCandidateView(
      @NonNull FeedstockBatchProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "feedstock batch profile target candidate cannot be null");
    return new FeedstockPlanningTargetCandidateView(
        candidate.currentMaterialStateId(),
        candidate.characteristics(),
        candidate.distance(),
        candidate.contributionRole()
    );
  }

  private record TargetCandidates(
      @Nullable FeedstockPlanningTargetAnalysisView analysis,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockPlanningTargetCandidateView> candidatesByBatchId
  ) {

    private TargetCandidates {
      candidatesByBatchId = Map.copyOf(requireNonNull(candidatesByBatchId, "target candidates cannot be null"));
    }

    private static @NonNull TargetCandidates empty() {
      return new TargetCandidates(null, Map.of());
    }
  }
}
