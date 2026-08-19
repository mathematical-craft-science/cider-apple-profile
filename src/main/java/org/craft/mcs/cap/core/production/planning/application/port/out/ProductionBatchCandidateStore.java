package org.craft.mcs.cap.core.production.planning.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;


/** Persistence boundary for production candidates and their immutable analysis metadata. */
public interface ProductionBatchCandidateStore {

  @NonNull ProductionBatchCandidateSnapshot create(@NonNull ProductionBatchCandidateSnapshot snapshot);

  @NonNull ProductionBatchCandidateSnapshot update(@NonNull ProductionBatchCandidateSnapshot snapshot);

  @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
      @NonNull ProductionBatchCandidateId candidateId
  );
}
