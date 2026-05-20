package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


@Table("feedstock_measurements")
public class FeedstockMeasurementJdbcEntity {

  @Id
  @NonNull
  @Column("measurement_id")
  UUID measurementId;

  @NonNull
  @Column("batch_id")
  AggregateReference<FeedstockBatchJdbcEntity, UUID> batch;

  @NonNull
  @Column("name")
  String name;

  @Nullable
  @Column("measured_at")
  Instant measuredAt;

  @Nullable
  @Column("note")
  String note;

  @Nullable @Column("sugar_kind") MeasurementKind sugarKind;
  @Nullable @Column("sugar_exact_sg") BigDecimal sugarExactValue;
  @Nullable @Column("sugar_min_sg") BigDecimal sugarMinValue;
  @Nullable @Column("sugar_max_sg") BigDecimal sugarMaxValue;
  @Nullable @Column("sugar_mean_sg") BigDecimal sugarMeanValue;
  @Nullable @Column("sugar_median_sg") BigDecimal sugarMedianValue;
  @Nullable @Column("sugar_standard_deviation") BigDecimal sugarStandardDeviation;
  @Nullable @Column("sugar_sample_size") Integer sugarSampleSize;

  @Nullable @Column("ph_kind") MeasurementKind phKind;
  @Nullable @Column("ph_exact") BigDecimal phExactValue;
  @Nullable @Column("ph_min") BigDecimal phMinValue;
  @Nullable @Column("ph_max") BigDecimal phMaxValue;
  @Nullable @Column("ph_mean") BigDecimal phMeanValue;
  @Nullable @Column("ph_median") BigDecimal phMedianValue;
  @Nullable @Column("ph_standard_deviation") BigDecimal phStandardDeviation;
  @Nullable @Column("ph_sample_size") Integer phSampleSize;

  @Nullable @Column("ta_kind") MeasurementKind titratableAcidityKind;
  @Nullable @Column("ta_exact_g_l_as_malic") BigDecimal titratableAcidityExactValue;
  @Nullable @Column("ta_min_g_l_as_malic") BigDecimal titratableAcidityMinValue;
  @Nullable @Column("ta_max_g_l_as_malic") BigDecimal titratableAcidityMaxValue;
  @Nullable @Column("ta_mean_g_l_as_malic") BigDecimal titratableAcidityMeanValue;
  @Nullable @Column("ta_median_g_l_as_malic") BigDecimal titratableAcidityMedianValue;
  @Nullable @Column("ta_standard_deviation") BigDecimal titratableAcidityStandardDeviation;
  @Nullable @Column("ta_sample_size") Integer titratableAciditySampleSize;

  @Nullable @Column("tanninity_kind") MeasurementKind tanninityKind;
  @Nullable @Column("tanninity_exact_mg_l_tae") BigDecimal tanninityExactValue;
  @Nullable @Column("tanninity_min_mg_l_tae") BigDecimal tanninityMinValue;
  @Nullable @Column("tanninity_max_mg_l_tae") BigDecimal tanninityMaxValue;
  @Nullable @Column("tanninity_mean_mg_l_tae") BigDecimal tanninityMeanValue;
  @Nullable @Column("tanninity_median_mg_l_tae") BigDecimal tanninityMedianValue;
  @Nullable @Column("tanninity_standard_deviation") BigDecimal tanninityStandardDeviation;
  @Nullable @Column("tanninity_sample_size") Integer tanninitySampleSize;

  @Nullable
  @Column("chemical_profile_source")
  String chemicalProfileSource;

  @Nullable
  @Column("chemical_profile_confidence")
  ConfidenceLevel chemicalProfileConfidence;

  @CreatedDate
  @Nullable
  @Column("created_at")
  Instant createdAt;

  @LastModifiedDate
  @Nullable
  @Column("updated_at")
  Instant updatedAt;

  @Version
  @Column("version")
  long version;

  @SuppressWarnings("NullAway.Init")
  public FeedstockMeasurementJdbcEntity() {
  }

  FeedstockMeasurementJdbcEntity(
      @NonNull UUID measurementId,
      @NonNull UUID batchId,
      @NonNull String name,
      @NonNull Instant measuredAt,
      @Nullable String note,
      @Nullable MeasurementJdbcColumns sugar,
      @Nullable MeasurementJdbcColumns ph,
      @Nullable MeasurementJdbcColumns titratableAcidity,
      @Nullable MeasurementJdbcColumns tanninity,
      @Nullable String chemicalProfileSource,
      @Nullable ConfidenceLevel chemicalProfileConfidence
  ) {
    this.measurementId = requireNonNull(measurementId, "feedstock measurement id cannot be null");
    this.batch = AggregateReference.to(requireNonNull(batchId, "feedstock batch id cannot be null"));
    this.name = requireNonNull(name, "feedstock measurement name cannot be null");
    this.measuredAt = requireNonNull(measuredAt, "measured at cannot be null");
    this.note = normalize(note);
    this.chemicalProfileSource = normalize(chemicalProfileSource);
    this.chemicalProfileConfidence = chemicalProfileConfidence;
    applySugar(sugar);
    applyPh(ph);
    applyTitratableAcidity(titratableAcidity);
    applyTanninity(tanninity);
  }

  @Nullable MeasurementJdbcColumns sugar() {
    return measurement(sugarKind, sugarExactValue, sugarMinValue, sugarMaxValue, sugarMeanValue,
        sugarMedianValue, sugarStandardDeviation, sugarSampleSize);
  }

  @Nullable MeasurementJdbcColumns ph() {
    return measurement(phKind, phExactValue, phMinValue, phMaxValue, phMeanValue,
        phMedianValue, phStandardDeviation, phSampleSize);
  }

  @Nullable MeasurementJdbcColumns titratableAcidity() {
    return measurement(titratableAcidityKind, titratableAcidityExactValue, titratableAcidityMinValue,
        titratableAcidityMaxValue, titratableAcidityMeanValue, titratableAcidityMedianValue,
        titratableAcidityStandardDeviation, titratableAciditySampleSize);
  }

  @Nullable MeasurementJdbcColumns tanninity() {
    return measurement(tanninityKind, tanninityExactValue, tanninityMinValue, tanninityMaxValue,
        tanninityMeanValue, tanninityMedianValue, tanninityStandardDeviation, tanninitySampleSize);
  }

  private void applySugar(@Nullable MeasurementJdbcColumns measurement) {
    sugarKind = kind(measurement);
    sugarExactValue = exactValue(measurement);
    sugarMinValue = minValue(measurement);
    sugarMaxValue = maxValue(measurement);
    sugarMeanValue = meanValue(measurement);
    sugarMedianValue = medianValue(measurement);
    sugarStandardDeviation = standardDeviation(measurement);
    sugarSampleSize = sampleSize(measurement);
  }

  private void applyPh(@Nullable MeasurementJdbcColumns measurement) {
    phKind = kind(measurement);
    phExactValue = exactValue(measurement);
    phMinValue = minValue(measurement);
    phMaxValue = maxValue(measurement);
    phMeanValue = meanValue(measurement);
    phMedianValue = medianValue(measurement);
    phStandardDeviation = standardDeviation(measurement);
    phSampleSize = sampleSize(measurement);
  }

  private void applyTitratableAcidity(@Nullable MeasurementJdbcColumns measurement) {
    titratableAcidityKind = kind(measurement);
    titratableAcidityExactValue = exactValue(measurement);
    titratableAcidityMinValue = minValue(measurement);
    titratableAcidityMaxValue = maxValue(measurement);
    titratableAcidityMeanValue = meanValue(measurement);
    titratableAcidityMedianValue = medianValue(measurement);
    titratableAcidityStandardDeviation = standardDeviation(measurement);
    titratableAciditySampleSize = sampleSize(measurement);
  }

  private void applyTanninity(@Nullable MeasurementJdbcColumns measurement) {
    tanninityKind = kind(measurement);
    tanninityExactValue = exactValue(measurement);
    tanninityMinValue = minValue(measurement);
    tanninityMaxValue = maxValue(measurement);
    tanninityMeanValue = meanValue(measurement);
    tanninityMedianValue = medianValue(measurement);
    tanninityStandardDeviation = standardDeviation(measurement);
    tanninitySampleSize = sampleSize(measurement);
  }

  private static @Nullable MeasurementKind kind(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.kind;
  }

  private static @Nullable BigDecimal exactValue(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.exactValue;
  }

  private static @Nullable BigDecimal minValue(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.minValue;
  }

  private static @Nullable BigDecimal maxValue(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.maxValue;
  }

  private static @Nullable BigDecimal meanValue(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.meanValue;
  }

  private static @Nullable BigDecimal medianValue(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.medianValue;
  }

  private static @Nullable BigDecimal standardDeviation(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.standardDeviation;
  }

  private static @Nullable Integer sampleSize(@Nullable MeasurementJdbcColumns measurement) {
    return measurement == null ? null : measurement.sampleSize;
  }

  private static @Nullable MeasurementJdbcColumns measurement(
      @Nullable MeasurementKind kind,
      @Nullable BigDecimal exactValue,
      @Nullable BigDecimal minValue,
      @Nullable BigDecimal maxValue,
      @Nullable BigDecimal meanValue,
      @Nullable BigDecimal medianValue,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize
  ) {
    if (kind == null) {
      return null;
    }

    final var measurement = new MeasurementJdbcColumns();
    measurement.kind = kind;
    measurement.exactValue = exactValue;
    measurement.minValue = minValue;
    measurement.maxValue = maxValue;
    measurement.meanValue = meanValue;
    measurement.medianValue = medianValue;
    measurement.standardDeviation = standardDeviation;
    measurement.sampleSize = sampleSize;
    return measurement;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
