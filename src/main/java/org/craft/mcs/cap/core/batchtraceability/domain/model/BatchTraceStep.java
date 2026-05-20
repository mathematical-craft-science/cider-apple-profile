package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.time.Instant;
import java.util.List;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Append-only business fact in batch traceability.
 *
 * <p>The step records what happened, which subjects were inputs and outputs, and optional material
 * snapshots captured at that moment. It does not enforce feedstock or production invariants; those
 * stay in their owning aggregates.
 */
@Entity
public record BatchTraceStep(
    @NonNull BatchTraceId batchTraceId,
    @Identity @NonNull BatchTraceStepId stepId,
    @Nullable BatchTraceStepId parentStepId,
    @NonNull BatchTraceOperationType operationType,
    @NonNull List<@NonNull BatchTraceStepSubject> inputs,
    @NonNull List<@NonNull BatchTraceStepSubject> outputs,
    @NonNull Instant occurredAt,
    @Nullable String performedBy,
    @Nullable String note
) {

  public BatchTraceStep {
    requireNonNull(batchTraceId, "batch trace id cannot be null");
    requireNonNull(stepId, "batch trace step id cannot be null");
    requireNonNull(operationType, "batch trace operation type cannot be null");
    inputs = List.copyOf(requireNonNull(inputs, "batch trace step inputs cannot be null"));
    outputs = List.copyOf(requireNonNull(outputs, "batch trace step outputs cannot be null"));
    requireNonNull(occurredAt, "batch trace step occurred at cannot be null");
    performedBy = normalize(performedBy);
    note = normalize(note);

    if (outputs.isEmpty()) {
      throw new DomainRuleViolationException("batch trace step must contain at least one output subject");
    }
  }

  public static @NonNull BatchTraceStep create(
      @NonNull BatchTraceId batchTraceId,
      @Nullable BatchTraceStepId parentStepId,
      @NonNull BatchTraceOperationType operationType,
      @NonNull List<@NonNull BatchTraceStepSubject> inputs,
      @NonNull List<@NonNull BatchTraceStepSubject> outputs,
      @NonNull Instant occurredAt,
      @Nullable String performedBy,
      @Nullable String note
  ) {
    return new BatchTraceStep(
        batchTraceId,
        BatchTraceStepId.newId(),
        parentStepId,
        operationType,
        inputs,
        outputs,
        occurredAt,
        performedBy,
        note
    );
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }
    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
