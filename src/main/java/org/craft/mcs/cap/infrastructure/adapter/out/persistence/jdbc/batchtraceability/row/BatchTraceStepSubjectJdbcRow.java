package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
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
  public UUID subjectId;

  @Nullable
  public String displayNameSnapshot;

  @Nullable
  public BigDecimal snapshotVolumeLiters;

  @Nullable
  public String snapshotConfidence;

  @Nullable
  public BigDecimal sugarValue;

  @Nullable
  public AxisValueKind sugarValueKind;

  @Nullable
  public BigDecimal sugarMin;

  @Nullable
  public BigDecimal sugarMax;

  @Nullable
  public BigDecimal acidityValue;

  @Nullable
  public AxisValueKind acidityValueKind;

  @Nullable
  public BigDecimal acidityMin;

  @Nullable
  public BigDecimal acidityMax;

  @Nullable
  public BigDecimal tanninValue;

  @Nullable
  public AxisValueKind tanninValueKind;

  @Nullable
  public BigDecimal tanninMin;

  @Nullable
  public BigDecimal tanninMax;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenValue;

  @Nullable
  public AxisValueKind yeastAssimilableNitrogenValueKind;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenMin;

  @Nullable
  public BigDecimal yeastAssimilableNitrogenMax;

  @SuppressWarnings("NullAway.Init")
  public BatchTraceStepSubjectJdbcRow() {
  }

  public BatchTraceStepSubjectJdbcRow(
      @NonNull BatchTraceStepSubjectRole role,
      int subjectOrder,
      @NonNull UUID traceStateId,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull UUID subjectId,
      @Nullable String displayNameSnapshot
  ) {
    this.role = requireNonNull(role, "batch trace subject role cannot be null");
    this.subjectOrder = subjectOrder;
    this.traceStateId = requireNonNull(traceStateId, "batch trace state id cannot be null");
    this.subjectType = requireNonNull(subjectType, "batch trace subject type cannot be null");
    this.subjectId = requireNonNull(subjectId, "batch trace subject id cannot be null");
    this.displayNameSnapshot = normalize(displayNameSnapshot);
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
