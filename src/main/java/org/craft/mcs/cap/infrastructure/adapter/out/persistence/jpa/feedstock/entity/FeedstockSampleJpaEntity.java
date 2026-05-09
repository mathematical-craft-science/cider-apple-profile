package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaEmbeddable;
import org.craft.mcs.cap.shared.domain.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "feedstock_samples")
@EntityListeners(AuditingEntityListener.class)
public class FeedstockSampleJpaEntity {

  @Id
  @NonNull
  @Column(name = "sample_id", nullable = false, updatable = false)
  UUID sampleId;

  @NonNull
  @Column(name = "cider_maker_id", nullable = false, updatable = false)
  UUID ciderMakerId;

  @NonNull
  @Column(name = "batch_id", nullable = false, updatable = false)
  UUID batchId;

  @NonNull
  @Column(name = "name", nullable = false, length = 160)
  String name;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 32)
  FeedstockType type;

  @Nullable
  @Column(name = "measured_at", nullable = false)
  Instant measuredAt;

  @Nullable
  @Column(name = "note", length = 2000)
  String note;

  @Nullable
  @Embedded
  @AttributeOverrides(
      {
          @AttributeOverride(name = "kind", column = @Column(name = "sugar_kind")),
          @AttributeOverride(name = "exactValue", column = @Column(name = "sugar_exact_sg")),
          @AttributeOverride(name = "minValue", column = @Column(name = "sugar_min_sg")),
          @AttributeOverride(name = "maxValue", column = @Column(name = "sugar_max_sg")),
          @AttributeOverride(name = "meanValue", column = @Column(name = "sugar_mean_sg")),
          @AttributeOverride(name = "medianValue", column = @Column(name = "sugar_median_sg")),
          @AttributeOverride(name = "standardDeviation", column = @Column(name = "sugar_standard_deviation")),
          @AttributeOverride(name = "sampleSize", column = @Column(name = "sugar_sample_size"))
      }
  )
  MeasurementJpaEmbeddable sugar;

  @Nullable
  @Embedded
  @AttributeOverrides(
      {
          @AttributeOverride(name = "kind", column = @Column(name = "ph_kind")),
          @AttributeOverride(name = "exactValue", column = @Column(name = "ph_exact")),
          @AttributeOverride(name = "minValue", column = @Column(name = "ph_min")),
          @AttributeOverride(name = "maxValue", column = @Column(name = "ph_max")),
          @AttributeOverride(name = "meanValue", column = @Column(name = "ph_mean")),
          @AttributeOverride(name = "medianValue", column = @Column(name = "ph_median")),
          @AttributeOverride(name = "standardDeviation", column = @Column(name = "ph_standard_deviation")),
          @AttributeOverride(name = "sampleSize", column = @Column(name = "ph_sample_size"))
      }
  )
  MeasurementJpaEmbeddable ph;

  @Nullable
  @Embedded
  @AttributeOverrides(
      {
          @AttributeOverride(name = "kind", column = @Column(name = "ta_kind")),
          @AttributeOverride(name = "exactValue", column = @Column(name = "ta_exact_g_l_as_malic")),
          @AttributeOverride(name = "minValue", column = @Column(name = "ta_min_g_l_as_malic")),
          @AttributeOverride(name = "maxValue", column = @Column(name = "ta_max_g_l_as_malic")),
          @AttributeOverride(name = "meanValue", column = @Column(name = "ta_mean_g_l_as_malic")),
          @AttributeOverride(name = "medianValue", column = @Column(name = "ta_median_g_l_as_malic")),
          @AttributeOverride(name = "standardDeviation", column = @Column(name = "ta_standard_deviation")),
          @AttributeOverride(name = "sampleSize", column = @Column(name = "ta_sample_size"))
      }
  )
  MeasurementJpaEmbeddable titratableAcidity;

  @Nullable
  @Embedded
  @AttributeOverrides(
      {
          @AttributeOverride(name = "kind", column = @Column(name = "tanninity_kind")),
          @AttributeOverride(name = "exactValue", column = @Column(name = "tanninity_exact_mg_l_tae")),
          @AttributeOverride(name = "minValue", column = @Column(name = "tanninity_min_mg_l_tae")),
          @AttributeOverride(name = "maxValue", column = @Column(name = "tanninity_max_mg_l_tae")),
          @AttributeOverride(name = "meanValue", column = @Column(name = "tanninity_mean_mg_l_tae")),
          @AttributeOverride(name = "medianValue", column = @Column(name = "tanninity_median_mg_l_tae")),
          @AttributeOverride(name = "standardDeviation", column = @Column(name = "tanninity_standard_deviation")),
          @AttributeOverride(name = "sampleSize", column = @Column(name = "tanninity_sample_size"))
      }
  )
  MeasurementJpaEmbeddable tanninity;

  @Nullable
  @Column(name = "chemical_profile_source", length = 255)
  String chemicalProfileSource;

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "chemical_profile_confidence", length = 32)
  ConfidenceLevel chemicalProfileConfidence;

  @NonNull
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  Instant createdAt;

  @NonNull
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  long version;

  @SuppressWarnings("NullAway.Init")
  protected FeedstockSampleJpaEntity() {
  }

  FeedstockSampleJpaEntity(
      @NonNull UUID sampleId,
      @NonNull UUID ciderMakerId,
      @NonNull UUID batchId,
      @NonNull String name,
      @NonNull FeedstockType type,
      @NonNull Instant measuredAt,
      @Nullable String note,
      @Nullable MeasurementJpaEmbeddable sugar,
      @Nullable MeasurementJpaEmbeddable ph,
      @Nullable MeasurementJpaEmbeddable titratableAcidity,
      @Nullable MeasurementJpaEmbeddable tanninity,
      @Nullable String chemicalProfileSource,
      @Nullable ConfidenceLevel chemicalProfileConfidence
  ) {
    this.sampleId = requireNonNull(sampleId, "feedstock sample id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.name = requireNonNull(name, "feedstock sample name cannot be null");
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.measuredAt = requireNonNull(measuredAt, "measured at cannot be null");
    this.note = note;
    this.sugar = sugar;
    this.ph = ph;
    this.titratableAcidity = titratableAcidity;
    this.tanninity = tanninity;
    this.chemicalProfileSource = normalize(chemicalProfileSource);
    this.chemicalProfileConfidence = chemicalProfileConfidence;
  }

  @NonNull
  UUID id() {
    return requireNonNull(sampleId, "feedstock sample id cannot be null");
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
