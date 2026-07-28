package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningTargetCandidateView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Application read model for the feedstock planning.
 *
 * <p>This query service deliberately composes batch metadata, current material-state evidence, graph
 * projection, target candidate scoring, ABV, YAN and fermentation-start readiness into one
 * application read model. It is not a domain aggregate; its responsibility is to give API clients a
 * coherent planning snapshot without forcing them to stitch several business endpoints
 * together.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockPlanningBatchesService implements QueryFeedstockPlanningBatchesUseCase {

  private final @NonNull ListFeedstockBatchesUseCase listFeedstockBatchesUseCase;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull QueryFeedstockBatchProfileTargetCandidatesUseCase queryTargetCandidatesUseCase;
  private final @NonNull FeedstockCurrentProfileAssessmentService currentProfileAssessmentService;

  @Override
  public @NonNull FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query) {
    requireNonNull(query, "feedstock planning batch view query cannot be null");

    final var batches = listFeedstockBatchesUseCase.list(new ListFeedstockBatchesQuery(
        query.workspaceId(),
        query.season()
    ));
    final var measurementsByBatchId = currentProfileQueryService.findCurrentProfiles(batches)
        .stream()
        .collect(Collectors.toMap(
            FeedstockCurrentProfileView::batchId,
            Function.identity()
        ));
    final var targetCandidates = targetCandidates(query);
    return new FeedstockPlanningView(
        batches.stream()
            .map(batch -> toBatchView(
                batch,
                measurementsByBatchId.get(batch.batchId()),
                targetCandidates.candidatesByBatchId().get(batch.batchId())
            ))
            .toList(),
        targetCandidates.analysis()
    );
  }

  private @NonNull FeedstockPlanningBatchView toBatchView(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile,
      @Nullable FeedstockPlanningTargetCandidateView targetCandidate
  ) {
    final var coherentTargetCandidate = targetCandidateFor(currentProfile, targetCandidate);
    return new FeedstockPlanningBatchView(
        batch,
        currentProfile == null ? null : currentProfile.summary(),
        currentProfileAssessmentService.assess(batch, currentProfile),
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
        new FeedstockAnalysisCandidateFilters(null, query.season(), null, null),
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
