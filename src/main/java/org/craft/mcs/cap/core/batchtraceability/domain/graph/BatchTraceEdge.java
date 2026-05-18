package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceEdge(
    @NonNull BatchTraceStepId stepId,
    @NonNull BatchTraceOperationType operationType,
    @NonNull List<@NonNull BatchTraceStepSubject> inputs,
    @NonNull List<@NonNull BatchTraceStepSubject> outputs
) {

  public BatchTraceEdge {
    requireNonNull(stepId, "batch trace edge step id cannot be null");
    requireNonNull(operationType, "batch trace edge operation type cannot be null");
    inputs = List.copyOf(requireNonNull(inputs, "batch trace edge inputs cannot be null"));
    outputs = List.copyOf(requireNonNull(outputs, "batch trace edge outputs cannot be null"));

    if (outputs.isEmpty()) {
      throw new IllegalArgumentException("batch trace edge must contain at least one output subject");
    }
  }
}
