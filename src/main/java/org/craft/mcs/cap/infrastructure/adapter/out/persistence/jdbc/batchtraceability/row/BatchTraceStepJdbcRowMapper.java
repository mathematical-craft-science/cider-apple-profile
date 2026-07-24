package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStateId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
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
        step.workspaceId().value(),
        stepIdValue(step.parentStepId()),
        step.operationType(),
        step.occurredAt(),
        step.performedBy().type(),
        step.performedBy().id(),
        step.note()
    );
    addSubjects(row, BatchTraceStepSubjectRole.INPUT, step.inputs());
    addSubjects(row, BatchTraceStepSubjectRole.OUTPUT, step.outputs());

    return row;
  }

  public static @NonNull BatchTraceStep toDomain(@NonNull BatchTraceStepJdbcRow row) {
    requireNonNull(row, "batch trace step row cannot be null");

    return new BatchTraceStep(
        WorkspaceId.of(requireNonNull(row.workspaceId, "batch trace workspace id cannot be null")),
        BatchTraceStepId.of(requireNonNull(row.stepId, "batch trace step id cannot be null")),
        stepId(row.parentStepId),
        requireNonNull(row.operationType, "batch trace operation type cannot be null"),
        toSubjects(row, BatchTraceStepSubjectRole.INPUT),
        toSubjects(row, BatchTraceStepSubjectRole.OUTPUT),
        requireNonNull(row.occurredAt, "batch trace occurred at cannot be null"),
        new BatchTraceActorReference(
            requireNonNull(row.performedByType, "batch trace actor type cannot be null"),
            requireNonNull(row.performedById, "batch trace actor id cannot be null")
        ),
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
          subject.traceStateId().value(),
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
        BatchTraceStateId.of(requireNonNull(row.traceStateId, "batch trace state id cannot be null")),
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

    row.snapshotVolumeLiters = volumeLiters(snapshot.volume());
    row.snapshotConfidence = confidenceName(snapshot.confidence());
    applyAxis(row, ChemicalAxis.SUGAR, axisValue(snapshot.profile(), ChemicalAxis.SUGAR));
    applyAxis(row, ChemicalAxis.ACIDITY, axisValue(snapshot.profile(), ChemicalAxis.ACIDITY));
    applyAxis(row, ChemicalAxis.TANNIN, axisValue(snapshot.profile(), ChemicalAxis.TANNIN));
    applyAxis(
        row,
        ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
        axisValue(snapshot.profile(), ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN)
    );
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
      }
      case ACIDITY -> {
        row.acidityValue = value.value();
        row.acidityValueKind = value.valueKind();
        row.acidityMin = value.min();
        row.acidityMax = value.max();
      }
      case TANNIN -> {
        row.tanninValue = value.value();
        row.tanninValueKind = value.valueKind();
        row.tanninMin = value.min();
        row.tanninMax = value.max();
      }
      case YEAST_ASSIMILABLE_NITROGEN -> {
        row.yeastAssimilableNitrogenValue = value.value();
        row.yeastAssimilableNitrogenValueKind = value.valueKind();
        row.yeastAssimilableNitrogenMin = value.min();
        row.yeastAssimilableNitrogenMax = value.max();
      }
    }
  }

  private static @Nullable BatchTraceMaterialSnapshot toSnapshot(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    final var volume = volumeValue(row.snapshotVolumeLiters);
    final var profile = toProfile(row);

    if (volume == null && profile == null && row.snapshotConfidence == null) {
      return null;
    }

    return new BatchTraceMaterialSnapshot(volume, profile, confidenceLevel(row.snapshotConfidence));
  }

  private static @Nullable ConfidenceLevel confidenceLevel(@Nullable String value) {
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  private static @Nullable ChemicalProfileAxes toProfile(@NonNull BatchTraceStepSubjectJdbcRow row) {
    final var sugar = toAxisValue(
        ChemicalAxis.SUGAR,
        row.sugarValue,
        row.sugarValueKind,
        row.sugarMin,
        row.sugarMax
    );
    final var acidity = toAxisValue(
        ChemicalAxis.ACIDITY,
        row.acidityValue,
        row.acidityValueKind,
        row.acidityMin,
        row.acidityMax
    );
    final var tannin = toAxisValue(
        ChemicalAxis.TANNIN,
        row.tanninValue,
        row.tanninValueKind,
        row.tanninMin,
        row.tanninMax
    );
    final var yeastAssimilableNitrogen = toAxisValue(
        ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
        row.yeastAssimilableNitrogenValue,
        row.yeastAssimilableNitrogenValueKind,
        row.yeastAssimilableNitrogenMin,
        row.yeastAssimilableNitrogenMax
    );

    return sugar == null && acidity == null && tannin == null && yeastAssimilableNitrogen == null
        ? null
        : ChemicalProfileAxes.of(sugar, acidity, tannin, yeastAssimilableNitrogen);
  }

  private static @Nullable AxisValue toAxisValue(
      @NonNull ChemicalAxis axis,
      @Nullable BigDecimal value,
      @Nullable AxisValueKind valueKind,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
    if (value == null) {
      return null;
    }

    return new AxisValue(
        axis,
        value,
        requireNonNull(valueKind, "batch trace axis value kind cannot be null"),
        min,
        max
    );
  }

  private static @Nullable UUID stepIdValue(@Nullable BatchTraceStepId stepId) {
    return stepId == null ? null : stepId.value();
  }

  private static @Nullable BatchTraceStepId stepId(@Nullable UUID value) {
    return value == null ? null : BatchTraceStepId.of(value);
  }

  private static @Nullable BigDecimal volumeLiters(@Nullable VolumeValue volume) {
    return volume == null ? null : volume.liters();
  }

  private static @Nullable VolumeValue volumeValue(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static @Nullable String confidenceName(@Nullable ConfidenceLevel confidence) {
    return confidence == null ? null : confidence.name();
  }

  private static @Nullable AxisValue axisValue(
      @Nullable ChemicalProfileAxes profile,
      @NonNull ChemicalAxis axis
  ) {
    if (profile == null) {
      return null;
    }

    final var value = profile.valueOf(axis);
    return value.orElse(null); // NOSONAR
  }
}
