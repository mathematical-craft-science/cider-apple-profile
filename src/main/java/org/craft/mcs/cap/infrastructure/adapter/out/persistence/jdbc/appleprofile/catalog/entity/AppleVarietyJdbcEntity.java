package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog.entity;

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
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


@Table("apple_varieties")
public class AppleVarietyJdbcEntity {

  @Id
  @NonNull
  @Column("variety_id")
  UUID varietyId;

  @NonNull
  @Column("name")
  String name;

  @Nullable @Column("sugar_kind") MeasurementKind sugarKind;
  @Nullable @Column("sugar_exact_sg") BigDecimal sugarExactValue;
  @Nullable @Column("sugar_min_sg") BigDecimal sugarMinValue;
  @Nullable @Column("sugar_max_sg") BigDecimal sugarMaxValue;

  @Nullable @Column("ph_kind") MeasurementKind phKind;
  @Nullable @Column("ph_exact") BigDecimal phExactValue;
  @Nullable @Column("ph_min") BigDecimal phMinValue;
  @Nullable @Column("ph_max") BigDecimal phMaxValue;

  @Nullable @Column("ta_kind") MeasurementKind titratableAcidityKind;
  @Nullable @Column("ta_exact_g_l_as_malic") BigDecimal titratableAcidityExactValue;
  @Nullable @Column("ta_min_g_l_as_malic") BigDecimal titratableAcidityMinValue;
  @Nullable @Column("ta_max_g_l_as_malic") BigDecimal titratableAcidityMaxValue;

  @Nullable @Column("tannin_kind") MeasurementKind tanninKind;
  @Nullable @Column("tannin_exact_mg_l_tae") BigDecimal tanninExactValue;
  @Nullable @Column("tannin_min_mg_l_tae") BigDecimal tanninMinValue;
  @Nullable @Column("tannin_max_mg_l_tae") BigDecimal tanninMaxValue;

  @Nullable
  @Column("chemical_profile_source")
  String referenceProfileSource;

  @Nullable
  @Column("chemical_profile_confidence")
  ConfidenceLevel referenceProfileConfidence;

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
  public AppleVarietyJdbcEntity() {
  }

  public AppleVarietyJdbcEntity(
      @NonNull UUID varietyId,
      @NonNull String name,
      @Nullable MeasurementJdbcColumns sugar,
      @Nullable MeasurementJdbcColumns ph,
      @Nullable MeasurementJdbcColumns titratableAcidity,
      @Nullable MeasurementJdbcColumns tannin,
      @Nullable String referenceProfileSource,
      @Nullable ConfidenceLevel referenceProfileConfidence
  ) {
    this.varietyId = requireNonNull(varietyId, "variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.referenceProfileSource = normalize(referenceProfileSource);
    this.referenceProfileConfidence = referenceProfileConfidence;
    applySugar(sugar);
    applyPh(ph);
    applyTitratableAcidity(titratableAcidity);
    applyTannin(tannin);
  }

  @Nullable
  MeasurementJdbcColumns sugar() {
    return measurement(
        sugarKind,
        sugarExactValue,
        sugarMinValue,
        sugarMaxValue
    );
  }

  @Nullable
  MeasurementJdbcColumns ph() {
    return measurement(
        phKind,
        phExactValue,
        phMinValue,
        phMaxValue
    );
  }

  @Nullable
  MeasurementJdbcColumns titratableAcidity() {
    return measurement(
        titratableAcidityKind,
        titratableAcidityExactValue,
        titratableAcidityMinValue,
        titratableAcidityMaxValue
    );
  }

  @Nullable
  MeasurementJdbcColumns tannin() {
    return measurement(
        tanninKind,
        tanninExactValue,
        tanninMinValue,
        tanninMaxValue
    );
  }

  private void applySugar(@Nullable MeasurementJdbcColumns measurement) {
    sugarKind = kind(measurement);
    sugarExactValue = exactValue(measurement);
    sugarMinValue = minValue(measurement);
    sugarMaxValue = maxValue(measurement);
  }

  private void applyPh(@Nullable MeasurementJdbcColumns measurement) {
    phKind = kind(measurement);
    phExactValue = exactValue(measurement);
    phMinValue = minValue(measurement);
    phMaxValue = maxValue(measurement);
  }

  private void applyTitratableAcidity(@Nullable MeasurementJdbcColumns measurement) {
    titratableAcidityKind = kind(measurement);
    titratableAcidityExactValue = exactValue(measurement);
    titratableAcidityMinValue = minValue(measurement);
    titratableAcidityMaxValue = maxValue(measurement);
  }

  private void applyTannin(@Nullable MeasurementJdbcColumns measurement) {
    tanninKind = kind(measurement);
    tanninExactValue = exactValue(measurement);
    tanninMinValue = minValue(measurement);
    tanninMaxValue = maxValue(measurement);
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

  private static @Nullable MeasurementJdbcColumns measurement(
      @Nullable MeasurementKind kind,
      @Nullable BigDecimal exactValue,
      @Nullable BigDecimal minValue,
      @Nullable BigDecimal maxValue
  ) {
    if (kind == null) {
      return null;
    }

    final var measurement = new MeasurementJdbcColumns();
    measurement.kind = kind;
    measurement.exactValue = exactValue;
    measurement.minValue = minValue;
    measurement.maxValue = maxValue;
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
