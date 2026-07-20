package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.BatchTraceabilityPersistenceMappingException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public class BatchTraceStepSubjectJdbcRow {

  @NonNull
  public BatchTraceStepSubjectRole role;

  public int subjectOrder;

  @NonNull
  public UUID traceStateId;

  @NonNull
  public BatchTraceSubjectType subjectType;

  @NonNull
  public String subjectId;

  @Nullable
  public String displayNameSnapshot;

  @Nullable
  public BigDecimal snapshotVolumeLiters;

  @Nullable
  public String snapshotConfidence;

  @Nullable
  public BigDecimal sugarValue;

  @Nullable
  public MeasurementValueKind sugarValueKind;

  @Nullable
  public BigDecimal sugarMin;

  @Nullable
  public BigDecimal sugarMax;

  @Nullable
  public BigDecimal sugarStandardDeviation;

  @Nullable
  public Integer sugarSampleSize;

  @Nullable
  public BigDecimal acidityValue;

  @Nullable
  public MeasurementValueKind acidityValueKind;

  @Nullable
  public BigDecimal acidityMin;

  @Nullable
  public BigDecimal acidityMax;

  @Nullable
  public BigDecimal acidityStandardDeviation;

  @Nullable
  public Integer aciditySampleSize;

  @Nullable
  public BigDecimal tanninityValue;

  @Nullable
  public MeasurementValueKind tanninityValueKind;

  @Nullable
  public BigDecimal tanninityMin;

  @Nullable
  public BigDecimal tanninityMax;

  @Nullable
  public BigDecimal tanninityStandardDeviation;

  @Nullable
  public Integer tanninitySampleSize;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenValue;

  @Nullable
  public MeasurementValueKind yeastAssimilableNitrogenValueKind;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenMin;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenMax;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenStandardDeviation;

  @Nullable
  public Integer yeastAssimilableNitrogenSampleSize;

  @SuppressWarnings("NullAway.Init")
  public BatchTraceStepSubjectJdbcRow() {
  }

  public BatchTraceStepSubjectJdbcRow(
      @NonNull BatchTraceStepSubjectRole role,
      int subjectOrder,
      @NonNull UUID traceStateId,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull String subjectId,
      @Nullable String displayNameSnapshot
  ) {
    this.role = requireNonNull(role, "batch trace subject role cannot be null");
    this.subjectOrder = subjectOrder;
    this.traceStateId = requireNonNull(traceStateId, "batch trace state id cannot be null");
    this.subjectType = requireNonNull(subjectType, "batch trace subject type cannot be null");
    this.subjectId = normalizeRequired(subjectId, "batch trace subject id cannot be blank");
    this.displayNameSnapshot = normalize(displayNameSnapshot);
  }

  private static @NonNull String normalizeRequired(
      @NonNull String value,
      @NonNull String message
  ) {
    final var normalized = requireNonNull(value, message).strip();
    if (normalized.isEmpty()) {
      throw new BatchTraceabilityPersistenceMappingException(message);
    }
    return normalized;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
