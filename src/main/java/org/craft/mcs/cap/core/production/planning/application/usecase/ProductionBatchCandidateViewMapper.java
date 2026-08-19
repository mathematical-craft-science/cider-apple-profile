package org.craft.mcs.cap.core.production.planning.application.usecase;

import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Projects the persisted candidate snapshot into an application read model. */
public final class ProductionBatchCandidateViewMapper {

  private ProductionBatchCandidateViewMapper() {
  }

  public static @NonNull ProductionBatchCandidateView toView(
      @NonNull ProductionBatchCandidateSnapshot snapshot
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    final var candidate = snapshot.candidate();
    return new ProductionBatchCandidateView(
        candidate.candidateId(),
        candidate.workspaceId(),
        candidate.status(),
        candidate.sourceComposition(),
        candidate.profile(),
        snapshot.potentialAbv(),
        snapshot.normalizerCode(),
        snapshot.targetDistance(),
        snapshot.warnings(),
        candidate.confirmedProductionBatchId()
    );
  }
}
