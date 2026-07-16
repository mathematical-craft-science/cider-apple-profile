package org.craft.mcs.cap.core.production.candidate.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;


/** Persistence boundary for production candidates and their immutable analysis metadata. */
public interface ProductionBatchCandidateStore {

  @NonNull ProductionBatchCandidateSnapshot save(@NonNull ProductionBatchCandidateSnapshot snapshot);

  @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
      @NonNull ProductionBatchCandidateId candidateId
  );
}
