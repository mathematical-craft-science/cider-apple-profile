package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

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


public final class BatchTraceStepJdbcRowMapper {

  private BatchTraceStepJdbcRowMapper() {
  }

  public static @NonNull BatchTraceStepJdbcRow fromDomain(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var row = new BatchTraceStepJdbcRow(
        step.stepId().value(),
        step.batchTraceId().value(),
        step.parentStepId() == null ? null : step.parentStepId().value(), // NOSONAR
        step.operationType(),
        step.occurredAt(),
        step.performedBy(),
        step.note()
    );
    addSubjects(row, BatchTraceStepSubjectRole.INPUT, step.inputs());
    addSubjects(row, BatchTraceStepSubjectRole.OUTPUT, step.outputs());

    return row;
  }

  public static @NonNull BatchTraceStep toDomain(@NonNull BatchTraceStepJdbcRow row) {
    requireNonNull(row, "batch trace step row cannot be null");

    return new BatchTraceStep(
        BatchTraceId.of(requireNonNull(row.traceId, "batch trace id cannot be null")),
        BatchTraceStepId.of(requireNonNull(row.stepId, "batch trace step id cannot be null")),
        row.parentStepId == null ? null : BatchTraceStepId.of(row.parentStepId), // NOSONAR
        requireNonNull(row.operationType, "batch trace operation type cannot be null"),
        toSubjects(row, BatchTraceStepSubjectRole.INPUT),
        toSubjects(row, BatchTraceStepSubjectRole.OUTPUT),
        requireNonNull(row.occurredAt, "batch trace occurred at cannot be null"),
        row.performedBy,
        row.note
    );
  }

  private static void addSubjects(
      @NonNull BatchTraceStepJdbcRow row,
      @NonNull BatchTraceStepSubjectRole role,
      @NonNull List<@NonNull BatchTraceStepSubject> subjects
  ) {
    for (var index = 0; index < subjects.size(); index++) {
      final var subject = subjects.get(index);
      final var subjectRow = new BatchTraceStepSubjectJdbcRow(
          role,
          index,
          subject.subject().type(),
          subject.subject().id(),
          subject.subject().displayNameSnapshot()
      );
      applySnapshot(subjectRow, subject.snapshot());
      row.addSubject(subjectRow);
    }
  }

  private static @NonNull List<@NonNull BatchTraceStepSubject> toSubjects(
      @NonNull BatchTraceStepJdbcRow row,
      @NonNull BatchTraceStepSubjectRole role
  ) {
    return row.subjects.stream()
        .filter(subject -> subject.role == role)
        .sorted(Comparator.comparingInt(subject -> subject.subjectOrder))
        .map(BatchTraceStepJdbcRowMapper::toSubject)
        .toList();
  }

  private static @NonNull BatchTraceStepSubject toSubject(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    return new BatchTraceStepSubject(
        new BatchTraceSubject(
            requireNonNull(row.subjectType, "batch trace subject type cannot be null"),
            requireNonNull(row.subjectId, "batch trace subject id cannot be null"),
            row.displayNameSnapshot
        ),
        toSnapshot(row)
    );
  }

  private static void applySnapshot(
      @NonNull BatchTraceStepSubjectJdbcRow row,
      @Nullable BatchTraceMaterialSnapshot snapshot
  ) {
    if (snapshot == null) {
      return;
    }

    row.snapshotVolumeLiters = snapshot.volume() == null ? null : snapshot.volume().liters(); // NOSONAR
    row.snapshotConfidence = snapshot.confidence();
    applyAxis(row, ChemicalAxis.SUGAR, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.SUGAR).orElse(null)); // NOSONAR
    applyAxis(row, ChemicalAxis.ACIDITY, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.ACIDITY).orElse(null)); // NOSONAR
    applyAxis(row, ChemicalAxis.TANNINITY, snapshot.profile() == null
        ? null
        : snapshot.profile().valueOf(ChemicalAxis.TANNINITY).orElse(null)); // NOSONAR
  }

  private static void applyAxis(
      @NonNull BatchTraceStepSubjectJdbcRow row,
      @NonNull ChemicalAxis axis,
      @Nullable AxisValue value
  ) {
    if (value == null) {
      return;
    }

    switch (axis) {
      case SUGAR -> {
        row.sugarValue = value.value();
        row.sugarValueKind = value.valueKind();
        row.sugarMin = value.min();
        row.sugarMax = value.max();
        row.sugarStandardDeviation = value.standardDeviation();
        row.sugarSampleSize = value.sampleSize();
      }
      case ACIDITY -> {
        row.acidityValue = value.value();
        row.acidityValueKind = value.valueKind();
        row.acidityMin = value.min();
        row.acidityMax = value.max();
        row.acidityStandardDeviation = value.standardDeviation();
        row.aciditySampleSize = value.sampleSize();
      }
      case TANNINITY -> {
        row.tanninityValue = value.value();
        row.tanninityValueKind = value.valueKind();
        row.tanninityMin = value.min();
        row.tanninityMax = value.max();
        row.tanninityStandardDeviation = value.standardDeviation();
        row.tanninitySampleSize = value.sampleSize();
      }
    }
  }

  private static @Nullable BatchTraceMaterialSnapshot toSnapshot(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    final var volume = row.snapshotVolumeLiters == null
        ? null
        : VolumeValue.ofLiters(row.snapshotVolumeLiters); // NOSONAR
    final var profile = toProfile(row);

    if (volume == null && profile == null && row.snapshotConfidence == null) {
      return null;
    }

    return new BatchTraceMaterialSnapshot(volume, profile, row.snapshotConfidence);
  }

  private static @Nullable ChemicalProfileAxes toProfile(@NonNull BatchTraceStepSubjectJdbcRow row) {
    final var sugar = toAxisValue(
        ChemicalAxis.SUGAR,
        row.sugarValue,
        row.sugarValueKind,
        row.sugarMin,
        row.sugarMax,
        row.sugarStandardDeviation,
        row.sugarSampleSize
    );
    final var acidity = toAxisValue(
        ChemicalAxis.ACIDITY,
        row.acidityValue,
        row.acidityValueKind,
        row.acidityMin,
        row.acidityMax,
        row.acidityStandardDeviation,
        row.aciditySampleSize
    );
    final var tanninity = toAxisValue(
        ChemicalAxis.TANNINITY,
        row.tanninityValue,
        row.tanninityValueKind,
        row.tanninityMin,
        row.tanninityMax,
        row.tanninityStandardDeviation,
        row.tanninitySampleSize
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
