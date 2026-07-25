package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.batchtraceability.application.InconsistentMaterialStateHistoryException;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Reconstructs one continuous current-state chain per business subject from immutable trace facts. */
final class CurrentMaterialStateReconstructor {

  private static final Comparator<SubjectIdentity> SUBJECT_ORDER =
      Comparator.comparing(SubjectIdentity::type)
          .thenComparing(SubjectIdentity::id);

  private CurrentMaterialStateReconstructor() {
  }

  static @NonNull List<@NonNull CurrentMaterialState> reconstruct(
      @NonNull WorkspaceId workspaceId,
      @NonNull List<@NonNull BatchTraceStep> steps
  ) {
    requireNonNull(workspaceId, "material state workspace id cannot be null");
    requireNonNull(steps, "batch trace steps cannot be null");
    final Map<SubjectIdentity, SubjectHistory> histories = new LinkedHashMap<>();
    final Set<MaterialStateId> outputStateIds = new HashSet<>();

    for (final var step : steps) {
      requireWorkspace(workspaceId, step);
      addStep(histories, outputStateIds, step);
    }

    return histories.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(SUBJECT_ORDER))
        .map(entry -> entry.getValue().resolve(workspaceId))
        .toList();
  }

  private static void requireWorkspace(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceStep step
  ) {
    if (!step.workspaceId().equals(workspaceId)) {
      throw inconsistent("trace step belongs to another workspace");
    }
  }

  private static void addStep(
      @NonNull Map<@NonNull SubjectIdentity, @NonNull SubjectHistory> histories,
      @NonNull Set<@NonNull MaterialStateId> outputStateIds,
      @NonNull BatchTraceStep step
  ) {
    final var inputs = groupBySubject(step.inputs());
    final var outputs = groupBySubject(step.outputs());
    final var subjects = Stream.concat(inputs.keySet().stream(), outputs.keySet().stream())
        .collect(Collectors.toUnmodifiableSet());

    for (final var subject : subjects) {
      final var subjectInputs = inputs.getOrDefault(subject, List.of());
      final var subjectOutputs = outputs.getOrDefault(subject, List.of());
      if (subjectInputs.size() > 1 || subjectOutputs.size() != 1) {
        throw inconsistent("each trace step must contain at most one input and exactly one output per subject");
      }

      final var output = subjectOutputs.getFirst();
      if (!outputStateIds.add(output.traceStateId())) {
        throw inconsistent("an output state is defined more than once");
      }
      final var history = histories.computeIfAbsent(subject, ignored -> new SubjectHistory());
      history.addOutput(output);
      if (!subjectInputs.isEmpty()) {
        history.addTransition(subjectInputs.getFirst(), output);
      }
    }
  }

  private static @NonNull Map<@NonNull SubjectIdentity, @NonNull List<@NonNull BatchTraceStepSubject>>
      groupBySubject(@NonNull List<@NonNull BatchTraceStepSubject> states) {
    final Map<SubjectIdentity, List<BatchTraceStepSubject>> grouped = new LinkedHashMap<>();
    for (final var state : states) {
      grouped.computeIfAbsent(SubjectIdentity.of(state.subject()), ignored -> new ArrayList<>())
          .add(state);
    }
    return grouped;
  }

  private static @NonNull InconsistentMaterialStateHistoryException inconsistent(
      @NonNull String reason
  ) {
    return new InconsistentMaterialStateHistoryException(reason);
  }

  private static final class SubjectHistory {

    private final Map<MaterialStateId, BatchTraceSubject> outputs = new LinkedHashMap<>();
    private final Map<MaterialStateId, MaterialStateId> nextByInput = new HashMap<>();

    private void addOutput(@NonNull BatchTraceStepSubject output) {
      if (outputs.putIfAbsent(output.traceStateId(), output.subject()) != null) {
        throw inconsistent("a subject state is defined more than once");
      }
    }

    private void addTransition(
        @NonNull BatchTraceStepSubject input,
        @NonNull BatchTraceStepSubject output
    ) {
      if (input.traceStateId().equals(output.traceStateId())) {
        throw inconsistent("a transition must create a new material state");
      }
      if (nextByInput.putIfAbsent(input.traceStateId(), output.traceStateId()) != null) {
        throw inconsistent("a material state is consumed by more than one transition");
      }
    }

    private @NonNull CurrentMaterialState resolve(@NonNull WorkspaceId workspaceId) {
      if (!outputs.keySet().containsAll(nextByInput.keySet())) {
        throw inconsistent("a transition input does not reference an earlier output state");
      }

      final var roots = new HashSet<>(outputs.keySet());
      roots.removeAll(nextByInput.values());
      final var terminals = new HashSet<>(outputs.keySet());
      terminals.removeAll(nextByInput.keySet());
      if (roots.size() != 1 || terminals.size() != 1) {
        throw inconsistent("a material subject must have one initial state and one terminal state");
      }

      final var terminal = terminals.iterator().next();
      final Set<MaterialStateId> visited = new HashSet<>();
      var current = roots.iterator().next();
      while (true) {
        if (!visited.add(current)) {
          throw inconsistent("a material subject state chain is cyclic");
        }
        final var next = nextByInput.get(current);
        if (next == null) {
          break;
        }
        current = next;
      }
      if (visited.size() != outputs.size() || !current.equals(terminal)) {
        throw inconsistent("a material subject state chain is disconnected");
      }

      return new CurrentMaterialState(
          workspaceId,
          requireNonNull(outputs.get(terminal), "terminal material state subject cannot be null"),
          terminal,
          nextByInput.size()
      );
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
