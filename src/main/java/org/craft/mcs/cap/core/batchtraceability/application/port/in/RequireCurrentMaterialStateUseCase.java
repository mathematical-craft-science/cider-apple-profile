package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Locks one material subject while a transactional dependent write validates or changes its evidence. */
public interface RequireCurrentMaterialStateUseCase {

  @NonNull CurrentMaterialState lockCurrent(@NonNull BatchTraceSubject subject);

  @NonNull CurrentMaterialState requireCurrent(
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId expectedStateId
  );
}
