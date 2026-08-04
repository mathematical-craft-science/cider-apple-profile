package org.craft.mcs.cap.core.traceability.domain.graph;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceEdge(
    @NonNull BatchTraceStepId stepId,
    @NonNull BatchTraceStepType stepType,
    @NonNull Instant occurredAt,
    @NonNull List<@NonNull BatchTraceStepSubject> inputs,
    @NonNull List<@NonNull BatchTraceStepSubject> outputs
) {

  public BatchTraceEdge {
    requireNonNull(stepId, "batch trace edge step id cannot be null");
    requireNonNull(stepType, "batch trace edge step type cannot be null");
    requireNonNull(occurredAt, "batch trace edge occurred at cannot be null");
    inputs = List.copyOf(requireNonNull(inputs, "batch trace edge inputs cannot be null"));
    outputs = List.copyOf(requireNonNull(outputs, "batch trace edge outputs cannot be null"));

    if (outputs.isEmpty()) {
      throw new DomainRuleViolationException("batch trace edge must contain at least one output subject");
    }
  }
}
