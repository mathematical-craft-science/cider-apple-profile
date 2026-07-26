package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public class BatchTraceStepSubjectJdbcRow {

  @NonNull
  public BatchTraceStepSubjectRole role;

  public int subjectOrder;

  @NonNull
  public UUID materialStateId;

  @NonNull
  public BatchTraceSubjectType subjectType;

  @NonNull
  public UUID subjectId;

  @Nullable
  public String displayNameSnapshot;

  @NonNull
  public Set<@NonNull MeasurementCharacteristic> invalidatedCharacteristics = new LinkedHashSet<>();

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
      @NonNull UUID materialStateId,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull UUID subjectId,
      @Nullable String displayNameSnapshot
  ) {
    this.role = requireNonNull(role, "batch trace subject role cannot be null");
    this.subjectOrder = subjectOrder;
    this.materialStateId = requireNonNull(materialStateId, "material state id cannot be null");
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
