package org.craft.mcs.cap.core.traceability.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Append-only business fact in batch traceability.
 *
 * <p>The step records what happened, which material states were inputs and outputs, and optional
 * snapshots captured at that moment. It does not enforce feedstock or production invariants; those
 * stay in their owning aggregates.
 * </p>
 */
@Entity
public record BatchTraceStep(
    @NonNull WorkspaceId workspaceId,
    @Identity @NonNull BatchTraceStepId stepId,
    @Nullable BatchTraceStepId parentStepId,
    @NonNull BatchTraceStepType stepType,
    @NonNull List<@NonNull BatchTraceStepSubject> inputs,
    @NonNull List<@NonNull BatchTraceStepSubject> outputs,
    @NonNull Instant occurredAt,
    @NonNull ActorReference performedBy,
    @Nullable String note
) {

  public BatchTraceStep {
    requireNonNull(workspaceId, "batch trace workspace id cannot be null");
    requireNonNull(stepId, "batch trace step id cannot be null");
    requireNonNull(stepType, "batch trace step type cannot be null");
    inputs = List.copyOf(requireNonNull(inputs, "batch trace step inputs cannot be null"));
    outputs = List.copyOf(requireNonNull(outputs, "batch trace step outputs cannot be null"));
    requireNonNull(occurredAt, "batch trace step occurred at cannot be null");
    requireNonNull(performedBy, "batch trace actor cannot be null");
    note = normalize(note);

    if (outputs.isEmpty()) {
      throw new DomainRuleViolationException("batch trace step must contain at least one output subject");
    }
    for (final var input : inputs) {
      if (!input.invalidatedCharacteristics().isEmpty()) {
        throw new DomainRuleViolationException(
            "measurement characteristics can be invalidated only by an output state"
        );
      }
    }
    final var inputStateIds = requireUniqueStateIds(inputs, "input");
    final var outputStateIds = requireUniqueStateIds(outputs, "output");
    requireUniqueSubjects(inputs, "input");
    requireUniqueSubjects(outputs, "output");
    requireNewOutputStates(inputStateIds, outputStateIds);
  }

  public static @NonNull BatchTraceStep create(
      @NonNull WorkspaceId workspaceId,
      @Nullable BatchTraceStepId parentStepId,
      @NonNull BatchTraceStepType stepType,
      @NonNull List<@NonNull BatchTraceStepSubject> inputs,
      @NonNull List<@NonNull BatchTraceStepSubject> outputs,
      @NonNull Instant occurredAt,
      @NonNull ActorReference performedBy,
      @Nullable String note
  ) {
    return new BatchTraceStep(
        workspaceId,
        BatchTraceStepId.newId(),
        parentStepId,
        stepType,
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

  private static @NonNull Set<@NonNull MaterialStateId> requireUniqueStateIds(
      @NonNull List<@NonNull BatchTraceStepSubject> states,
      @NonNull String role
  ) {
    final Set<MaterialStateId> stateIds = new HashSet<>();
    for (final var state : states) {
      if (!stateIds.add(state.materialStateId())) {
        throw new DomainRuleViolationException(
            "batch trace step " + role + " state ids must be unique"
        );
      }
    }
    return Set.copyOf(stateIds);
  }

  private static void requireNewOutputStates(
      @NonNull Set<@NonNull MaterialStateId> inputStateIds,
      @NonNull Set<@NonNull MaterialStateId> outputStateIds
  ) {
    for (final var outputStateId : outputStateIds) {
      if (inputStateIds.contains(outputStateId)) {
        throw new DomainRuleViolationException(
            "batch trace step outputs must identify new immutable material states"
        );
      }
    }
  }

  private static void requireUniqueSubjects(
      @NonNull List<@NonNull BatchTraceStepSubject> states,
      @NonNull String role
  ) {
    final Set<SubjectIdentity> subjects = new HashSet<>();
    for (final var state : states) {
      if (!subjects.add(SubjectIdentity.of(state.subject()))) {
        throw new DomainRuleViolationException(
            "batch trace step " + role + " subjects must be unique"
        );
      }
    }
  }

  private record SubjectIdentity(
      @NonNull BatchTraceSubjectType type,
      @NonNull UUID id
  ) {

    private static @NonNull SubjectIdentity of(@NonNull BatchTraceSubject subject) {
      return new SubjectIdentity(subject.type(), subject.id());
    }
  }
}
