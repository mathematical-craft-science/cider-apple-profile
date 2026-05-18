package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class BatchTraceStepJpaMapper {

  private BatchTraceStepJpaMapper() {
  }

  public static @NonNull BatchTraceStepJpaEntity fromDomain(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var entity = new BatchTraceStepJpaEntity(
        step.stepId().value(),
        step.batchTraceId().value(),
        step.parentStepId() == null ? null : step.parentStepId().value(), // NOSONAR
        step.operationType(),
        step.occurredAt(),
        step.performedBy(),
        step.note()
    );
    addSubjects(entity, BatchTraceStepSubjectRole.INPUT, step.inputs());
    addSubjects(entity, BatchTraceStepSubjectRole.OUTPUT, step.outputs());

    return entity;
  }

  public static @NonNull BatchTraceStep toDomain(@NonNull BatchTraceStepJpaEntity entity) {
    requireNonNull(entity, "batch trace step entity cannot be null");

    return new BatchTraceStep(
        BatchTraceId.of(requireNonNull(entity.traceId, "batch trace id cannot be null")),
        BatchTraceStepId.of(requireNonNull(entity.stepId, "batch trace step id cannot be null")),
        entity.parentStepId == null ? null : BatchTraceStepId.of(entity.parentStepId), // NOSONAR
        requireNonNull(entity.operationType, "batch trace operation type cannot be null"),
        toSubjects(entity, BatchTraceStepSubjectRole.INPUT),
        toSubjects(entity, BatchTraceStepSubjectRole.OUTPUT),
        requireNonNull(entity.occurredAt, "batch trace occurred at cannot be null"),
        entity.performedBy,
        entity.note
    );
  }

  private static void addSubjects(
      @NonNull BatchTraceStepJpaEntity entity,
      @NonNull BatchTraceStepSubjectRole role,
      @NonNull List<@NonNull BatchTraceStepSubject> subjects
  ) {
    for (var index = 0; index < subjects.size(); index++) {
      final var subject = subjects.get(index);
      final var subjectEntity = new BatchTraceStepSubjectJpaEntity(
          role,
          index,
          subject.subject().type(),
          subject.subject().id(),
          subject.subject().displayNameSnapshot()
      );
      applySnapshot(subjectEntity, subject.snapshot());
      entity.addSubject(subjectEntity);
    }
  }

  private static @NonNull List<@NonNull BatchTraceStepSubject> toSubjects(
      @NonNull BatchTraceStepJpaEntity entity,
      @NonNull BatchTraceStepSubjectRole role
  ) {
    return entity.subjects.stream()
        .filter(subject -> subject.role == role)
        .sorted(Comparator.comparingInt(subject -> subject.subjectOrder))
        .map(BatchTraceStepJpaMapper::toSubject)
        .toList();
  }

  private static @NonNull BatchTraceStepSubject toSubject(
      @NonNull BatchTraceStepSubjectJpaEntity entity
  ) {
    return new BatchTraceStepSubject(
        new BatchTraceSubject(
            requireNonNull(entity.subjectType, "batch trace subject type cannot be null"),
            requireNonNull(entity.subjectId, "batch trace subject id cannot be null"),
            entity.displayNameSnapshot
        ),
        toSnapshot(entity)
    );
  }

  private static void applySnapshot(
      @NonNull BatchTraceStepSubjectJpaEntity entity,
      @Nullable BatchTraceMaterialSnapshot snapshot
  ) {
    if (snapshot == null) {
      return;
    }

    entity.snapshotVolumeLiters = snapshot.volume() == null ? null : snapshot.volume().liters(); // NOSONAR
    entity.snapshotConfidence = snapshot.confidence();
    applyAxis(entity, ChemicalAxis.SUGAR, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.SUGAR).orElse(null)); // NOSONAR
    applyAxis(entity, ChemicalAxis.ACIDITY, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.ACIDITY).orElse(null)); // NOSONAR
    applyAxis(entity, ChemicalAxis.TANNINITY, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.TANNINITY).orElse(null)); // NOSONAR
  }

  private static void applyAxis(
      @NonNull BatchTraceStepSubjectJpaEntity entity,
      @NonNull ChemicalAxis axis,
      @Nullable AxisValue value
  ) {
    if (value == null) {
      return;
    }

    switch (axis) {
      case SUGAR -> {
        entity.sugarValue = value.value();
        entity.sugarValueKind = value.valueKind();
        entity.sugarMin = value.min();
        entity.sugarMax = value.max();
        entity.sugarStandardDeviation = value.standardDeviation();
        entity.sugarSampleSize = value.sampleSize();
      }
      case ACIDITY -> {
        entity.acidityValue = value.value();
        entity.acidityValueKind = value.valueKind();
        entity.acidityMin = value.min();
        entity.acidityMax = value.max();
        entity.acidityStandardDeviation = value.standardDeviation();
        entity.aciditySampleSize = value.sampleSize();
      }
      case TANNINITY -> {
        entity.tanninityValue = value.value();
        entity.tanninityValueKind = value.valueKind();
        entity.tanninityMin = value.min();
        entity.tanninityMax = value.max();
        entity.tanninityStandardDeviation = value.standardDeviation();
        entity.tanninitySampleSize = value.sampleSize();
      }
    }
  }

  private static @Nullable BatchTraceMaterialSnapshot toSnapshot(
      @NonNull BatchTraceStepSubjectJpaEntity entity
  ) {
    final var volume = entity.snapshotVolumeLiters == null
        ? null
        : VolumeValue.ofLiters(entity.snapshotVolumeLiters); // NOSONAR
    final var profile = toProfile(entity);

    if (volume == null && profile == null && entity.snapshotConfidence == null) {
      return null;
    }

    return new BatchTraceMaterialSnapshot(volume, profile, entity.snapshotConfidence);
  }

  private static @Nullable ChemicalProfileAxes toProfile(@NonNull BatchTraceStepSubjectJpaEntity entity) {
    final var sugar = toAxisValue(
        ChemicalAxis.SUGAR,
        entity.sugarValue,
        entity.sugarValueKind,
        entity.sugarMin,
        entity.sugarMax,
        entity.sugarStandardDeviation,
        entity.sugarSampleSize
    );
    final var acidity = toAxisValue(
        ChemicalAxis.ACIDITY,
        entity.acidityValue,
        entity.acidityValueKind,
        entity.acidityMin,
        entity.acidityMax,
        entity.acidityStandardDeviation,
        entity.aciditySampleSize
    );
    final var tanninity = toAxisValue(
        ChemicalAxis.TANNINITY,
        entity.tanninityValue,
        entity.tanninityValueKind,
        entity.tanninityMin,
        entity.tanninityMax,
        entity.tanninityStandardDeviation,
        entity.tanninitySampleSize
    );

    return sugar == null && acidity == null && tanninity == null
        ? null
        : ChemicalProfileAxes.of(sugar, acidity, tanninity);
  }

  private static @Nullable AxisValue toAxisValue(
      @NonNull ChemicalAxis axis,
      @Nullable BigDecimal value,
      @Nullable MeasurementValueKind valueKind,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize
  ) {
    if (value == null) {
      return null;
    }

    return new AxisValue(
        axis,
        value,
        requireNonNull(valueKind, "batch trace axis value kind cannot be null"),
        min,
        max,
        standardDeviation,
        sampleSize
    );
  }
}
