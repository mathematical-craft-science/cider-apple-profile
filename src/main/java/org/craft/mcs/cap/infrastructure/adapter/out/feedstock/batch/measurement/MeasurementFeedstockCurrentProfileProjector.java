package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.batch.measurement;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryCurrentMeasurementEvidenceUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetMetadataUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Projects generic Measurement state into Feedstock-owned current-profile views. */
@Component
@RequiredArgsConstructor
final class MeasurementFeedstockCurrentProfileProjector {

  private static final Set<MeasurementCharacteristic> PROFILE_CHARACTERISTICS = Set.copyOf(
      EnumSet.of(
          MeasurementCharacteristic.SPECIFIC_GRAVITY,
          MeasurementCharacteristic.TITRATABLE_ACIDITY,
          MeasurementCharacteristic.PH,
          MeasurementCharacteristic.TANNIN,
          MeasurementCharacteristic.YAN
      )
  );

  private final @NonNull QueryCurrentMeasurementEvidenceUseCase currentMeasurementEvidenceQuery;
  private final @NonNull QueryMeasurementSetMetadataUseCase measurementSetMetadataQuery;

  @NonNull List<@NonNull FeedstockCurrentProfileView> project(
      @NonNull List<@NonNull FeedstockCurrentProfileSubject> subjects
  ) {
    requireNonNull(subjects, "feedstock current-profile subjects cannot be null");
    if (subjects.isEmpty()) {
      return List.of();
    }
    final Map<FeedstockBatchId, CurrentMeasurementEvidenceView> evidenceByBatch = new LinkedHashMap<>();
    subjects.stream()
        .collect(Collectors.groupingBy(FeedstockCurrentProfileSubject::workspaceId))
        .forEach((workspaceId, workspaceSubjects) -> {
          final var measurementSubjects = workspaceSubjects.stream()
              .map(FeedstockCurrentProfileSubject::batchId)
              .map(MeasurementFeedstockCurrentProfileProjector::measurementSubject)
              .collect(Collectors.toUnmodifiableSet());
          currentMeasurementEvidenceQuery.getAllCurrent(workspaceId, measurementSubjects)
              .forEach((subject, evidence) -> evidenceByBatch.put(
                  FeedstockBatchId.of(subject.id()),
                  evidence
              ));
        });

    final var measurementSetIds = evidenceByBatch.values().stream()
        .flatMap(state -> state.characteristics().stream())
        .filter(characteristic -> PROFILE_CHARACTERISTICS.contains(characteristic.characteristic()))
        .map(CurrentMeasurementCharacteristicView::evidence)
        .map(MeasurementEvidenceView::evidenceReference)
        .map(MeasurementEvidenceReference::measurementSetId)
        .map(MeasurementSetId::of)
        .collect(Collectors.toUnmodifiableSet());
    final var metadataBySet = subjects.stream()
        .map(FeedstockCurrentProfileSubject::workspaceId)
        .distinct()
        .flatMap(workspaceId -> measurementSetMetadataQuery.findAll(workspaceId, measurementSetIds)
            .entrySet()
            .stream())
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    return subjects.stream()
        .map(FeedstockCurrentProfileSubject::batchId)
        .map(batchId -> toView(batchId, evidenceByBatch.get(batchId), metadataBySet))
        .flatMap(Optional::stream)
        .toList();
  }

  private static @NonNull Optional<@NonNull FeedstockCurrentProfileView> toView(
      @NonNull FeedstockBatchId batchId,
      @Nullable CurrentMeasurementEvidenceView currentEvidence,
      @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> metadataBySet
  ) {
    if (currentEvidence == null || currentEvidence.characteristics().isEmpty()) {
      return Optional.empty();
    }
    final var evidence = currentEvidence.characteristics().stream()
        .filter(characteristic -> PROFILE_CHARACTERISTICS.contains(characteristic.characteristic()))
        .map(CurrentMeasurementCharacteristicView::evidence)
        .toList();
    if (evidence.isEmpty()) {
      return Optional.empty();
    }
    final var anchor = evidence.stream()
        .max(Comparator
            .comparing(MeasurementEvidenceView::measuredAt)
            .thenComparing(item -> item.evidenceReference().recordId())
            .thenComparing(item -> item.evidenceReference().measurementSetId()))
        .orElseThrow();
    final var anchorReference = anchor.evidenceReference();
    final var metadata = requireNonNull(
        metadataBySet.get(MeasurementSetId.of(anchorReference.measurementSetId())),
        "measurement set metadata cannot be null"
    );
    return Optional.of(new FeedstockCurrentProfileView(
        batchId,
        currentEvidence.currentMaterialStateId(),
        currentEvidence.value(MeasurementCharacteristic.SPECIFIC_GRAVITY, SpecificGravityValue.class)
            .orElse(null), // NOSONAR
        currentEvidence.value(MeasurementCharacteristic.TITRATABLE_ACIDITY, TitratableAcidityValue.class)
            .orElse(null), // NOSONAR
        currentEvidence.value(MeasurementCharacteristic.PH, PhValue.class).orElse(null), // NOSONAR
        currentEvidence.value(MeasurementCharacteristic.TANNIN, TanninValue.class).orElse(null), // NOSONAR
        currentEvidence.value(MeasurementCharacteristic.YAN, YeastAssimilableNitrogenValue.class)
            .orElse(null), // NOSONAR
        evidence,
        new FeedstockLatestMeasurementSetView(
            anchorReference.measurementSetId(),
            metadata.name().value(),
            anchor.measuredAt(),
            metadata.version(),
            metadata.note()
        )
    ));
  }

  private static @NonNull MeasurementSubjectReference measurementSubject(
      @NonNull FeedstockBatchId batchId
  ) {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId.value());
  }
}
