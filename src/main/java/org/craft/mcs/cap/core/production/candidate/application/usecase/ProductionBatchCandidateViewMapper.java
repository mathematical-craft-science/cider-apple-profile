package org.craft.mcs.cap.core.production.candidate.application.usecase;

import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
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
