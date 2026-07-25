package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateAdvance;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateInitialization;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStatesTransition;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.TransitionMaterialStatesUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Applies a multi-subject material transition as one transactional traceability fact.
 *
 * <p>Existing subjects are advanced in stable identity order. Persistence adapters can therefore
 * lock only the affected pointers while concurrent transitions touching unrelated subjects
 * proceed independently.</p>
 */
public class TransitionMaterialStatesService implements TransitionMaterialStatesUseCase {

  private static final Comparator<BatchTraceSubject> SUBJECT_ORDER =
      Comparator.comparing(BatchTraceSubject::type)
          .thenComparing(BatchTraceSubject::id);

  private final @NonNull BatchTraceabilityStore traceabilityStore;
  private final @NonNull CurrentMaterialStateStore currentStateStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public TransitionMaterialStatesService(
      @NonNull BatchTraceabilityStore traceabilityStore,
      @NonNull CurrentMaterialStateStore currentStateStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.traceabilityStore = requireNonNull(traceabilityStore, "batch traceability store cannot be null");
    this.currentStateStore = requireNonNull(currentStateStore, "current material state store cannot be null");
    this.accessControl = requireNonNull(accessControl, "batch traceability access control cannot be null");
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void transition(@NonNull MaterialStatesTransition transition) {
    requireNonNull(transition, "material states transition cannot be null");
    final var workspaceId = transition.traceStep().workspaceId();
    final var subjectTypes = Stream.concat(
        transition.advances().stream()
            .map(MaterialStateAdvance::subject),
        transition.initializations().stream()
            .map(MaterialStateInitialization::subject)
    )
        .map(BatchTraceSubject::type)
        .collect(Collectors.toUnmodifiableSet());
    accessControl.requireWriteAccess(workspaceId, subjectTypes);

    transition.advances().stream()
        .sorted(Comparator.comparing(MaterialStateAdvance::subject, SUBJECT_ORDER))
        .forEach(advance -> currentStateStore.compareAndSet(
            workspaceId,
            advance.subject(),
            advance.expectedStateId(),
            advance.nextStateId()
        ));
    transition.initializations().stream()
        .sorted(Comparator.comparing(MaterialStateInitialization::subject, SUBJECT_ORDER))
        .forEach(initialization -> currentStateStore.initialize(
            workspaceId,
            initialization.subject(),
            initialization.initialStateId()
        ));
    traceabilityStore.append(transition.traceStep());
  }
}
