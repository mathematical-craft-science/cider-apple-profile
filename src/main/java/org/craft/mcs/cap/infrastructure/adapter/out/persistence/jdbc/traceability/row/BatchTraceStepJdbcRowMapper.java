package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialCharacteristics;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


public final class BatchTraceStepJdbcRowMapper {

  private BatchTraceStepJdbcRowMapper() {
  }

  public static @NonNull BatchTraceStepJdbcRow fromDomain(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var row = new BatchTraceStepJdbcRow(
        step.stepId().value(),
        step.workspaceId().value(),
        stepIdValue(step.parentStepId()),
        step.stepType(),
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
        requireNonNull(row.stepType, "batch trace step type cannot be null"),
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
          subject.materialStateId().value(),
          subject.subject().type(),
          subject.subject().id(),
          subject.subject().displayNameSnapshot()
      );
      subjectRow.invalidatedCharacteristics.addAll(subject.invalidatedCharacteristics());
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
        .map(BatchTraceStepJdbcRowMapper::toDomainSubject)
        .toList();
  }

  public static @NonNull BatchTraceStepSubject toDomainSubject(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    return new BatchTraceStepSubject(
        MaterialStateId.of(requireNonNull(row.materialStateId, "material state id cannot be null")),
        new BatchTraceSubject(
            requireNonNull(row.subjectType, "batch trace subject type cannot be null"),
            requireNonNull(row.subjectId, "batch trace subject id cannot be null"),
            row.displayNameSnapshot
        ),
        toSnapshot(row),
        row.invalidatedCharacteristics
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
    final var characteristics = snapshot.characteristics();
    if (characteristics == null) {
      return;
    }

    final var specificGravity = stored(characteristics.specificGravity());
    if (specificGravity != null) {
      row.specificGravityValue = specificGravity.value();
      row.specificGravityValueKind = specificGravity.kind();
      row.specificGravityMin = specificGravity.min();
      row.specificGravityMax = specificGravity.max();
    }
    final var titratableAcidity = stored(characteristics.titratableAcidity());
    if (titratableAcidity != null) {
      row.titratableAcidityValue = titratableAcidity.value();
      row.titratableAcidityValueKind = titratableAcidity.kind();
      row.titratableAcidityMin = titratableAcidity.min();
      row.titratableAcidityMax = titratableAcidity.max();
    }
    final var tannin = stored(characteristics.tannin());
    if (tannin != null) {
      row.tanninValue = tannin.value();
      row.tanninValueKind = tannin.kind();
      row.tanninMin = tannin.min();
      row.tanninMax = tannin.max();
    }
    final var yan = stored(characteristics.yeastAssimilableNitrogen());
    if (yan != null) {
      row.yeastAssimilableNitrogenValue = yan.value();
      row.yeastAssimilableNitrogenValueKind = yan.kind();
      row.yeastAssimilableNitrogenMin = yan.min();
      row.yeastAssimilableNitrogenMax = yan.max();
    }
  }

  private static @Nullable BatchTraceMaterialSnapshot toSnapshot(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    final var volume = volumeValue(row.snapshotVolumeLiters);
    final var characteristics = toCharacteristics(row);

    if (volume == null && characteristics == null && row.snapshotConfidence == null) {
      return null;
    }

    return new BatchTraceMaterialSnapshot(
        volume,
        characteristics,
        confidenceLevel(row.snapshotConfidence)
    );
  }

  private static @Nullable ConfidenceLevel confidenceLevel(@Nullable String value) {
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  private static @Nullable BatchTraceMaterialCharacteristics toCharacteristics(
      @NonNull BatchTraceStepSubjectJdbcRow row
  ) {
    final var specificGravity = toCharacteristic(
        row.specificGravityValue,
        row.specificGravityValueKind,
        row.specificGravityMin,
        row.specificGravityMax,
        SpecificGravityValue::of
    );
    final var titratableAcidity = toCharacteristic(
        row.titratableAcidityValue,
        row.titratableAcidityValueKind,
        row.titratableAcidityMin,
        row.titratableAcidityMax,
        TitratableAcidityValue::of
    );
    final var tannin = toCharacteristic(
        row.tanninValue,
        row.tanninValueKind,
        row.tanninMin,
        row.tanninMax,
        TanninValue::of
    );
    final var yeastAssimilableNitrogen = toCharacteristic(
        row.yeastAssimilableNitrogenValue,
        row.yeastAssimilableNitrogenValueKind,
        row.yeastAssimilableNitrogenMin,
        row.yeastAssimilableNitrogenMax,
        YeastAssimilableNitrogenValue::of
    );

    return specificGravity == null
        && titratableAcidity == null
        && tannin == null
        && yeastAssimilableNitrogen == null
        ? null
        : new BatchTraceMaterialCharacteristics(
            specificGravity,
            titratableAcidity,
            tannin,
            yeastAssimilableNitrogen
        );
  }

  private static <T extends MeasuredValue> @Nullable BatchTraceCharacteristicSnapshot<T> toCharacteristic(
      @Nullable BigDecimal value,
      @Nullable BatchTraceCharacteristicStorageKind storageKind,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max,
      @NonNull Function<@NonNull BigDecimal, @NonNull T> valueFactory
  ) {
    if (value == null) {
      return null;
    }

    return switch (requireNonNull(
        storageKind,
        "batch trace characteristic storage kind cannot be null"
    )) {
      case POINT -> BatchTraceCharacteristicSnapshot.direct(
          PointCharacteristicValue.of(valueFactory.apply(value))
      );
      case CALCULATED -> BatchTraceCharacteristicSnapshot.calculated(valueFactory.apply(value));
      case INTERVAL_MIDPOINT -> BatchTraceCharacteristicSnapshot.direct(
          IntervalCharacteristicValue.of(
              valueFactory.apply(requireNonNull(min, "batch trace interval minimum cannot be null")),
              valueFactory.apply(requireNonNull(max, "batch trace interval maximum cannot be null"))
          )
      );
    };
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

  private static <T extends MeasuredValue> @Nullable StoredTraceCharacteristic stored(
      @Nullable BatchTraceCharacteristicSnapshot<T> characteristic
  ) {
    if (characteristic == null) {
      return null;
    }

    return switch (characteristic) {
      case BatchTraceCharacteristicSnapshot.Calculated<T>(var value) -> new StoredTraceCharacteristic(
          value.asBigDecimal(),
          BatchTraceCharacteristicStorageKind.CALCULATED,
          null,
          null
      );
      case BatchTraceCharacteristicSnapshot.Direct<T>(var value) -> switch (value) {
        case PointCharacteristicValue<T>(var point) -> new StoredTraceCharacteristic(
            point.asBigDecimal(),
            BatchTraceCharacteristicStorageKind.POINT,
            null,
            null
        );
        case IntervalCharacteristicValue<T>(var lower, var upper) -> {
          final var minimum = lower.asBigDecimal();
          final var maximum = upper.asBigDecimal();
          yield new StoredTraceCharacteristic(
              midpoint(minimum, maximum),
              BatchTraceCharacteristicStorageKind.INTERVAL_MIDPOINT,
              minimum,
              maximum
          );
        }
      };
    };
  }

  private record StoredTraceCharacteristic(
      @NonNull BigDecimal value,
      @NonNull BatchTraceCharacteristicStorageKind kind,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
  }
}
