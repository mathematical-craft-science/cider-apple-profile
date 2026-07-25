package org.craft.mcs.cap.core.batchtraceability.application.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Atomic state transition described by one immutable trace step.
 *
 * <p>A transition may advance several existing material subjects and initialize several newly
 * created subjects. Every declared state must be represented by the trace step.</p>
 */
public record MaterialStatesTransition(
    @NonNull BatchTraceStep traceStep,
    @NonNull List<@NonNull MaterialStateAdvance> advances,
    @NonNull List<@NonNull MaterialStateInitialization> initializations
) {

  public MaterialStatesTransition {
    requireNonNull(traceStep, "material state transition trace step cannot be null");
    advances = List.copyOf(requireNonNull(advances, "material state advances cannot be null"));
    initializations = List.copyOf(
        requireNonNull(initializations, "material state initializations cannot be null")
    );
    if (advances.isEmpty() && initializations.isEmpty()) {
      throw new ApplicationValidationException("material state transition must change at least one subject");
    }
    requireUniqueSubjects(advances, initializations);
    requireUniqueInputStates(advances);
    requireUniqueOutputStates(advances, initializations);
    requireNewOutputStates(advances, initializations);
    advances.forEach(advance -> requireAdvanceReferences(traceStep, advance));
    initializations.forEach(initialization -> requireInitializationReference(traceStep, initialization));
    requireNoUndeclaredTraceStates(traceStep, advances, initializations);
  }

  private static void requireUniqueSubjects(
      @NonNull List<@NonNull MaterialStateAdvance> advances,
      @NonNull List<@NonNull MaterialStateInitialization> initializations
  ) {
    final Set<SubjectIdentity> identities = new HashSet<>();
    advances.forEach(advance -> addSubject(identities, advance.subject()));
    initializations.forEach(initialization -> addSubject(identities, initialization.subject()));
  }

  private static void addSubject(
      @NonNull Set<@NonNull SubjectIdentity> identities,
      @NonNull BatchTraceSubject subject
  ) {
    if (!identities.add(SubjectIdentity.of(subject))) {
      throw new ApplicationValidationException(
          "material state transition must declare each subject exactly once"
      );
    }
  }

  private static void requireUniqueOutputStates(
      @NonNull List<@NonNull MaterialStateAdvance> advances,
      @NonNull List<@NonNull MaterialStateInitialization> initializations
  ) {
    final Set<MaterialStateId> outputStateIds = new HashSet<>();
    advances.forEach(advance -> addOutputState(outputStateIds, advance.nextStateId()));
    initializations.forEach(initialization ->
        addOutputState(outputStateIds, initialization.initialStateId())
    );
  }

  private static void requireUniqueInputStates(
      @NonNull List<@NonNull MaterialStateAdvance> advances
  ) {
    final var inputStateIds = advances.stream()
        .map(MaterialStateAdvance::expectedStateId)
        .collect(Collectors.toUnmodifiableSet());
    if (inputStateIds.size() != advances.size()) {
      throw new ApplicationValidationException("material state input ids must be unique");
    }
  }

  private static void requireNewOutputStates(
      @NonNull List<@NonNull MaterialStateAdvance> advances,
      @NonNull List<@NonNull MaterialStateInitialization> initializations
  ) {
    final var inputStateIds = advances.stream()
        .map(MaterialStateAdvance::expectedStateId)
        .collect(Collectors.toUnmodifiableSet());
    final var reusesInputState = Stream.concat(
        advances.stream()
            .map(MaterialStateAdvance::nextStateId),
        initializations.stream()
            .map(MaterialStateInitialization::initialStateId)
    ).anyMatch(inputStateIds::contains);
    if (reusesInputState) {
      throw new ApplicationValidationException("material state outputs must use new state ids");
    }
  }

  private static void addOutputState(
      @NonNull Set<@NonNull MaterialStateId> outputStateIds,
      @NonNull MaterialStateId stateId
  ) {
    if (!outputStateIds.add(stateId)) {
      throw new ApplicationValidationException("material state output ids must be unique");
    }
  }

  private static void requireAdvanceReferences(
      @NonNull BatchTraceStep traceStep,
      @NonNull MaterialStateAdvance advance
  ) {
    if (!contains(traceStep.inputs(), advance.subject(), advance.expectedStateId())
        || !contains(traceStep.outputs(), advance.subject(), advance.nextStateId())) {
      throw new ApplicationValidationException(
          "material state advance must reference matching trace input and output states"
      );
    }
  }

  private static void requireInitializationReference(
      @NonNull BatchTraceStep traceStep,
      @NonNull MaterialStateInitialization initialization
  ) {
    if (!contains(traceStep.outputs(), initialization.subject(), initialization.initialStateId())) {
      throw new ApplicationValidationException(
          "material state initialization must reference a matching trace output state"
      );
    }
  }

  private static boolean contains(
      @NonNull Iterable<@NonNull BatchTraceStepSubject> states,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId stateId
  ) {
    for (final var state : states) {
      if (state.traceStateId().equals(stateId)
          && SubjectIdentity.of(state.subject()).equals(SubjectIdentity.of(subject))) {
        return true;
      }
    }
    return false;
  }

  private static void requireNoUndeclaredTraceStates(
      @NonNull BatchTraceStep traceStep,
      @NonNull List<@NonNull MaterialStateAdvance> advances,
      @NonNull List<@NonNull MaterialStateInitialization> initializations
  ) {
    final var declaredInputs = advances.stream()
        .map(advance -> StateIdentity.of(advance.subject(), advance.expectedStateId()))
        .collect(Collectors.toUnmodifiableSet());
    final var declaredOutputs = Stream.concat(
        advances.stream()
            .map(advance -> StateIdentity.of(advance.subject(), advance.nextStateId())),
        initializations.stream()
            .map(initialization ->
                StateIdentity.of(initialization.subject(), initialization.initialStateId())
            )
    ).collect(Collectors.toUnmodifiableSet());

    if (!matchesExactly(traceStep.inputs(), declaredInputs)
        || !matchesExactly(traceStep.outputs(), declaredOutputs)) {
      throw new ApplicationValidationException(
          "trace step states must exactly match the declared material state transition"
      );
    }
  }

  private static boolean matchesExactly(
      @NonNull List<@NonNull BatchTraceStepSubject> traceStates,
      @NonNull Set<@NonNull StateIdentity> declaredStates
  ) {
    final var actualStates = traceStates.stream()
        .map(StateIdentity::of)
        .collect(Collectors.toUnmodifiableSet());
    return traceStates.size() == actualStates.size() && actualStates.equals(declaredStates);
  }

  private record SubjectIdentity(
      @NonNull BatchTraceSubjectType type,
      @NonNull UUID id
  ) {

    private static @NonNull SubjectIdentity of(@NonNull BatchTraceSubject subject) {
      return new SubjectIdentity(subject.type(), subject.id());
    }
  }

  private record StateIdentity(
      @NonNull SubjectIdentity subject,
      @NonNull MaterialStateId stateId
  ) {

    private static @NonNull StateIdentity of(@NonNull BatchTraceStepSubject state) {
      return of(state.subject(), state.traceStateId());
    }

    private static @NonNull StateIdentity of(
        @NonNull BatchTraceSubject subject,
        @NonNull MaterialStateId stateId
    ) {
      return new StateIdentity(SubjectIdentity.of(subject), stateId);
    }
  }
}
