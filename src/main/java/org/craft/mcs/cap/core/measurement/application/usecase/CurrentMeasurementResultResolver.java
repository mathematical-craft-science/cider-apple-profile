package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.contract.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementMaterialStateLineage;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementMaterialStateLineageEntry;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementResultEvidenceView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateLineageProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementResultEvidenceQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;


/** Selects one current RESULT per characteristic from immutable evidence and material-state ancestry. */
@Component
@RequiredArgsConstructor
public final class CurrentMeasurementResultResolver {

  private static final Comparator<ApplicableResult> APPLICABILITY_ORDER =
      Comparator.comparingInt(ApplicableResult::depth)
          .thenComparing(ApplicableResult::measuredAt, reverseOrder())
          .thenComparing(ApplicableResult::recordIdOrder, reverseOrder());

  private final @NonNull MeasurementResultEvidenceQuery evidenceQuery;
  private final @NonNull MeasurementMaterialStateLineageProvider lineageProvider;

  public @NonNull List<@NonNull CurrentMeasurementResultView> resolve(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(currentStates, "current measurement states cannot be null");
    if (currentStates.isEmpty()) {
      return List.of();
    }

    final var lineages = lineageProvider.findLineages(workspaceId, currentStates);
    final var statesBySubject = lineages.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            entry -> entry.getValue().states().stream()
                .map(MeasurementMaterialStateLineageEntry::materialStateId)
                .collect(Collectors.toUnmodifiableSet())
        ));
    final var evidenceBySubject = groupBySubject(
        evidenceQuery.findByMaterialStates(workspaceId, statesBySubject)
    );
    return lineages.values().stream()
        .flatMap(lineage -> selectApplicableResults(
            lineage,
            evidenceBySubject.getOrDefault(lineage.subject(), List.of())
        ).stream())
        .toList();
  }

  private static @NonNull Map<@NonNull MeasurementSubjectReference,
      @NonNull List<@NonNull MeasurementResultEvidenceView>> groupBySubject(
      @NonNull List<@NonNull MeasurementResultEvidenceView> evidence
  ) {
    final Map<MeasurementSubjectReference, List<MeasurementResultEvidenceView>> grouped = new LinkedHashMap<>();
    evidence.forEach(result -> grouped
        .computeIfAbsent(result.subject(), _ -> new ArrayList<>())
        .add(result));
    return Map.copyOf(grouped);
  }

  private static @NonNull List<@NonNull CurrentMeasurementResultView> selectApplicableResults(
      @NonNull MeasurementMaterialStateLineage lineage,
      @NonNull List<@NonNull MeasurementResultEvidenceView> evidence
  ) {
    final var characteristics = evidence.stream()
        .map(MeasurementResultEvidenceView::characteristic)
        .distinct()
        .toList();
    return characteristics.stream()
        .map(characteristic -> applicableResult(lineage, evidence, characteristic))
        .flatMap(Optional::stream)
        .toList();
  }

  private static @NonNull Optional<@NonNull CurrentMeasurementResultView> applicableResult(
      @NonNull MeasurementMaterialStateLineage lineage,
      @NonNull List<@NonNull MeasurementResultEvidenceView> evidence,
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return lineage.states().stream()
        .filter(state -> !state.invalidatedCharacteristics().contains(characteristic))
        .flatMap(state -> evidence.stream()
            .filter(result -> result.materialStateId().equals(state.materialStateId()))
            .filter(result -> result.characteristic() == characteristic)
            .map(result -> new ApplicableResult(state.depth(), result)))
        .min(APPLICABILITY_ORDER)
        .map(result -> result.toCurrent(lineage.currentMaterialStateId()));
  }

  private record ApplicableResult(
      int depth,
      @NonNull MeasurementResultEvidenceView evidence
  ) {

    private @NonNull Instant measuredAt() {
      return evidence.measuredAt();
    }

    private @NonNull String recordIdOrder() {
      return evidence.recordId().value().toString();
    }

    private @NonNull CurrentMeasurementResultView toCurrent(
        @NonNull MaterialStateId currentMaterialStateId
    ) {
      return new CurrentMeasurementResultView(
          evidence.subject(),
          currentMaterialStateId,
          evidence.materialStateId(),
          evidence.measurementSetId(),
          evidence.recordId(),
          evidence.characteristic(),
          evidence.value(),
          evidence.measuredAt(),
          evidence.provenance()
      );
    }
  }
}
