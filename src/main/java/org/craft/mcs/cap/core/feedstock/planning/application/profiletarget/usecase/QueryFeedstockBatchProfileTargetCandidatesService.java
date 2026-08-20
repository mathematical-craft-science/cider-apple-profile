package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Analysis read model for target-aware feedstock batch candidates.
 *
 * <p>This use case is intentionally separate from the generic feedstock profile-target score
 * endpoint. It enriches distance-scored current batch profiles with contribution roles for
 * target-aware analysis clients where a technologist evaluates source material against a selected
 * target.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchProfileTargetCandidatesService
    implements QueryFeedstockBatchProfileTargetCandidatesUseCase {

  private final @NonNull ScoreFeedstockBatchProfileTargetUseCase scoreProfileTargetUseCase;

  @Override
  public @NonNull FeedstockBatchProfileTargetCandidates query(
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target candidate query cannot be null");
    final var scores = scoreProfileTargetUseCase.score(query);

    return new FeedstockBatchProfileTargetCandidates(
        query.analysisScenario(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> new FeedstockBatchProfileTargetCandidate(
                  score.batchId(),
                  score.currentMaterialStateId(),
                  score.profileEvidence(),
                  score.characteristics(),
                  score.distance(),
                  score.contribution()
              ))
            .toList()
    );
  }
}
