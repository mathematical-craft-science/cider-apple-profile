package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "batch_trace_step_subjects")
public class BatchTraceStepSubjectJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Nullable
  @Column(name = "subject_row_id")
  Long subjectRowId;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "step_id", nullable = false, updatable = false)
  BatchTraceStepJpaEntity step;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "subject_role", nullable = false, updatable = false, length = 16)
  BatchTraceStepSubjectRole role;

  @Column(name = "subject_order", nullable = false, updatable = false)
  int subjectOrder;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "subject_type", nullable = false, updatable = false, length = 64)
  BatchTraceSubjectType subjectType;

  @NonNull
  @Column(name = "subject_id", nullable = false, updatable = false, length = 120)
  String subjectId;

  @Nullable
  @Column(name = "display_name_snapshot", updatable = false, length = 255)
  String displayNameSnapshot;

  @Nullable
  @Column(name = "snapshot_volume_liters", updatable = false, precision = 15, scale = 3)
  BigDecimal snapshotVolumeLiters;

  @Nullable
  @Column(name = "snapshot_confidence", updatable = false, length = 64)
  String snapshotConfidence;

  @Nullable
  @Column(name = "sugar_value", updatable = false, precision = 12, scale = 9)
  BigDecimal sugarValue;

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "sugar_value_kind", updatable = false, length = 32)
  MeasurementValueKind sugarValueKind;

  @Nullable
  @Column(name = "sugar_min", updatable = false, precision = 12, scale = 9)
  BigDecimal sugarMin;

  @Nullable
  @Column(name = "sugar_max", updatable = false, precision = 12, scale = 9)
  BigDecimal sugarMax;

  @Nullable
  @Column(name = "sugar_standard_deviation", updatable = false, precision = 12, scale = 9)
  BigDecimal sugarStandardDeviation;

  @Nullable
  @Column(name = "sugar_sample_size", updatable = false)
  Integer sugarSampleSize;

  @Nullable
  @Column(name = "acidity_value", updatable = false, precision = 12, scale = 6)
  BigDecimal acidityValue;

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "acidity_value_kind", updatable = false, length = 32)
  MeasurementValueKind acidityValueKind;

  @Nullable
  @Column(name = "acidity_min", updatable = false, precision = 12, scale = 6)
  BigDecimal acidityMin;

  @Nullable
  @Column(name = "acidity_max", updatable = false, precision = 12, scale = 6)
  BigDecimal acidityMax;

  @Nullable
  @Column(name = "acidity_standard_deviation", updatable = false, precision = 12, scale = 6)
  BigDecimal acidityStandardDeviation;

  @Nullable
  @Column(name = "acidity_sample_size", updatable = false)
  Integer aciditySampleSize;

  @Nullable
  @Column(name = "tanninity_value", updatable = false, precision = 12, scale = 4)
  BigDecimal tanninityValue;

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "tanninity_value_kind", updatable = false, length = 32)
  MeasurementValueKind tanninityValueKind;

  @Nullable
  @Column(name = "tanninity_min", updatable = false, precision = 12, scale = 4)
  BigDecimal tanninityMin;

  @Nullable
  @Column(name = "tanninity_max", updatable = false, precision = 12, scale = 4)
  BigDecimal tanninityMax;

  @Nullable
  @Column(name = "tanninity_standard_deviation", updatable = false, precision = 12, scale = 4)
  BigDecimal tanninityStandardDeviation;

  @Nullable
  @Column(name = "tanninity_sample_size", updatable = false)
  Integer tanninitySampleSize;

  @SuppressWarnings("NullAway.Init")
  protected BatchTraceStepSubjectJpaEntity() {
  }

  BatchTraceStepSubjectJpaEntity(
      @NonNull BatchTraceStepSubjectRole role,
      int subjectOrder,
      @NonNull BatchTraceSubjectType subjectType,
      @NonNull String subjectId,
      @Nullable String displayNameSnapshot
  ) {
    this.role = requireNonNull(role, "batch trace subject role cannot be null");
    this.subjectOrder = subjectOrder;
    this.subjectType = requireNonNull(subjectType, "batch trace subject type cannot be null");
    this.subjectId = normalizeRequired(subjectId, "batch trace subject id cannot be blank");
    this.displayNameSnapshot = normalize(displayNameSnapshot);
  }

  private static @NonNull String normalizeRequired(@NonNull String value, @NonNull String message) {
    final var normalized = requireNonNull(value, message).strip();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(message);
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
