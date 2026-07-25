package org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.RequireCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves Measurement current-state tokens through the Batch Traceability boundary. */
@Component
@RequiredArgsConstructor
final class BatchTraceabilityMeasurementMaterialStateProvider implements MeasurementMaterialStateProvider {

  private final @NonNull GetCurrentMaterialStateUseCase getCurrentMaterialStateUseCase;
  private final @NonNull RequireCurrentMaterialStateUseCase requireCurrentMaterialStateUseCase;

  @Override
  public @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    return getCurrentMaterialStateUseCase.getCurrent(toTraceSubject(subject)).materialStateId();
  }

  @Override
  public @NonNull MaterialStateId lockCurrentState(@NonNull MeasurementSubjectReference subject) {
    requireNonNull(subject, "measurement subject cannot be null");
    return requireCurrentMaterialStateUseCase.lockCurrent(toTraceSubject(subject)).materialStateId();
  }

  @Override
  public @NonNull MaterialStateId requireCurrentState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId expectedStateId
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(expectedStateId, "expected material state id cannot be null");
    return requireCurrentMaterialStateUseCase.requireCurrent(
        toTraceSubject(subject),
        expectedStateId
    ).materialStateId();
  }

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subjects, "measurement subjects cannot be null");
    final var traceSubjectsByMeasurementSubject = subjects.stream()
        .collect(Collectors.toUnmodifiableMap(
            subject -> subject,
            BatchTraceabilityMeasurementMaterialStateProvider::toTraceSubject
        ));
    final var currentStates = getCurrentMaterialStateUseCase.getCurrent(
        workspaceId,
        Set.copyOf(traceSubjectsByMeasurementSubject.values())
    );
    return traceSubjectsByMeasurementSubject.entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            entry -> currentStates.get(entry.getValue()).materialStateId()
        ));
  }

  static @NonNull BatchTraceSubject toTraceSubject(@NonNull MeasurementSubjectReference subject) {
    return BatchTraceSubject.of(
        toTraceSubjectType(subject.type()),
        subject.id()
    );
  }

  private static @NonNull BatchTraceSubjectType toTraceSubjectType(
      @NonNull MeasurementSubjectType subjectType
  ) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
      case PRODUCTION_BATCH -> BatchTraceSubjectType.PRODUCTION_BATCH;
    };
  }
}
