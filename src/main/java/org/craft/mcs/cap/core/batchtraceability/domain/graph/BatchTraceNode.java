package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Read-model node representing one immutable historical state of a business material. */
@ValueObject
public record BatchTraceNode(
    @NonNull MaterialStateId traceStateId,
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot
) {

  public BatchTraceNode {
    requireNonNull(traceStateId, "batch trace node state id cannot be null");
    requireNonNull(subject, "batch trace node subject cannot be null");
  }

  public static @NonNull BatchTraceNode from(@NonNull BatchTraceStepSubject subject) {
    requireNonNull(subject, "batch trace step subject cannot be null");
    return new BatchTraceNode(
        subject.traceStateId(),
        subject.subject(),
        subject.snapshot()
    );
  }
}
