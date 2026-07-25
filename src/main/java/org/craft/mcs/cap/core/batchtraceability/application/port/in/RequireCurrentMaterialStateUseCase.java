package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Confirms and pins the expected current material state for a transactional dependent write. */
public interface RequireCurrentMaterialStateUseCase {

  @NonNull CurrentMaterialState requireCurrent(
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId expectedStateId
  );
}
