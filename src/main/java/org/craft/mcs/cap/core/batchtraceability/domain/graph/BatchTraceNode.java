package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceNode(
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot
) {

  public BatchTraceNode {
    requireNonNull(subject, "batch trace node subject cannot be null");
  }
}
