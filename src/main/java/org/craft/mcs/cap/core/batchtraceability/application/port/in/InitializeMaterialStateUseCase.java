package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Initializes the first current state of a newly created material subject. */
public interface InitializeMaterialStateUseCase {

  void initialize(
      @NonNull BatchTraceStep creationStep,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId initialStateId
  );
}
