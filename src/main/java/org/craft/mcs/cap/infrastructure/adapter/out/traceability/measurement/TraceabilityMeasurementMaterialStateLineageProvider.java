package org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementMaterialStateLineage;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementMaterialStateLineageEntry;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateLineageProvider;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryMaterialStateLineageUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Anti-corruption adapter translating Traceability ancestry into Measurement-owned lineage views. */
@Component
@RequiredArgsConstructor
public final class TraceabilityMeasurementMaterialStateLineageProvider
    implements MeasurementMaterialStateLineageProvider {

  private final @NonNull QueryMaterialStateLineageUseCase lineageQuery;

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MeasurementMaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(currentStates, "current measurement states cannot be null");
    final var traceStates = currentStates.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> toTraceSubject(entry.getKey()),
            Map.Entry::getValue
        ));
    return lineageQuery.findLineages(workspaceId, traceStates).values().stream()
        .map(TraceabilityMeasurementMaterialStateLineageProvider::toMeasurementLineage)
        .collect(Collectors.toUnmodifiableMap(
            MeasurementMaterialStateLineage::subject,
            lineage -> lineage
        ));
  }

  private static @NonNull MeasurementMaterialStateLineage toMeasurementLineage(
      @NonNull MaterialStateLineage lineage
  ) {
    return new MeasurementMaterialStateLineage(
        toMeasurementSubject(lineage.subject()),
        lineage.currentMaterialStateId(),
        lineage.states().stream()
            .map(state -> new MeasurementMaterialStateLineageEntry(
                state.materialStateId(),
                state.depth(),
                state.invalidatedCharacteristics()
            ))
            .toList()
    );
  }

  private static @NonNull BatchTraceSubject toTraceSubject(
      @NonNull MeasurementSubjectReference subject
  ) {
    return BatchTraceSubject.of(toTraceSubjectType(subject.type()), subject.id());
  }

  private static @NonNull BatchTraceSubjectType toTraceSubjectType(
      @NonNull MeasurementSubjectType subjectType
  ) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
      case PRODUCTION_BATCH -> BatchTraceSubjectType.PRODUCTION_BATCH;
    };
  }

  private static @NonNull MeasurementSubjectReference toMeasurementSubject(
      @NonNull BatchTraceSubject subject
  ) {
    return new MeasurementSubjectReference(
        switch (subject.type()) {
          case FEEDSTOCK_BATCH -> MeasurementSubjectType.FEEDSTOCK_BATCH;
          case PRODUCTION_BATCH -> MeasurementSubjectType.PRODUCTION_BATCH;
        },
        subject.id()
    );
  }
}
