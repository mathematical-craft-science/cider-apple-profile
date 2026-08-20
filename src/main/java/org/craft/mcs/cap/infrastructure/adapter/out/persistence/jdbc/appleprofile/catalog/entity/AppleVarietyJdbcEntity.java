package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueStorageKind;
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

  @Nullable @Column("sugar_kind") CharacteristicValueStorageKind sugarKind;
  @Nullable @Column("sugar_exact_sg") BigDecimal sugarExactValue;
  @Nullable @Column("sugar_min_sg") BigDecimal sugarMinValue;
  @Nullable @Column("sugar_max_sg") BigDecimal sugarMaxValue;

  @Nullable @Column("ph_kind") CharacteristicValueStorageKind phKind;
  @Nullable @Column("ph_exact") BigDecimal phExactValue;
  @Nullable @Column("ph_min") BigDecimal phMinValue;
  @Nullable @Column("ph_max") BigDecimal phMaxValue;

  @Nullable @Column("ta_kind") CharacteristicValueStorageKind titratableAcidityKind;
  @Nullable @Column("ta_exact_g_l_as_malic") BigDecimal titratableAcidityExactValue;
  @Nullable @Column("ta_min_g_l_as_malic") BigDecimal titratableAcidityMinValue;
  @Nullable @Column("ta_max_g_l_as_malic") BigDecimal titratableAcidityMaxValue;

  @Nullable @Column("tannin_kind") CharacteristicValueStorageKind tanninKind;
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
      @Nullable CharacteristicValueJdbcColumns sugar,
      @Nullable CharacteristicValueJdbcColumns ph,
      @Nullable CharacteristicValueJdbcColumns titratableAcidity,
      @Nullable CharacteristicValueJdbcColumns tannin,
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
  CharacteristicValueJdbcColumns sugar() {
    return characteristic(
        sugarKind,
        sugarExactValue,
        sugarMinValue,
        sugarMaxValue
    );
  }

  @Nullable
  CharacteristicValueJdbcColumns ph() {
    return characteristic(
        phKind,
        phExactValue,
        phMinValue,
        phMaxValue
    );
  }

  @Nullable
  CharacteristicValueJdbcColumns titratableAcidity() {
    return characteristic(
        titratableAcidityKind,
        titratableAcidityExactValue,
        titratableAcidityMinValue,
        titratableAcidityMaxValue
    );
  }

  @Nullable
  CharacteristicValueJdbcColumns tannin() {
    return characteristic(
        tanninKind,
        tanninExactValue,
        tanninMinValue,
        tanninMaxValue
    );
  }

  private void applySugar(@Nullable CharacteristicValueJdbcColumns characteristic) {
    sugarKind = kind(characteristic);
    sugarExactValue = exactValue(characteristic);
    sugarMinValue = minValue(characteristic);
    sugarMaxValue = maxValue(characteristic);
  }

  private void applyPh(@Nullable CharacteristicValueJdbcColumns characteristic) {
    phKind = kind(characteristic);
    phExactValue = exactValue(characteristic);
    phMinValue = minValue(characteristic);
    phMaxValue = maxValue(characteristic);
  }

  private void applyTitratableAcidity(@Nullable CharacteristicValueJdbcColumns characteristic) {
    titratableAcidityKind = kind(characteristic);
    titratableAcidityExactValue = exactValue(characteristic);
    titratableAcidityMinValue = minValue(characteristic);
    titratableAcidityMaxValue = maxValue(characteristic);
  }

  private void applyTannin(@Nullable CharacteristicValueJdbcColumns characteristic) {
    tanninKind = kind(characteristic);
    tanninExactValue = exactValue(characteristic);
    tanninMinValue = minValue(characteristic);
    tanninMaxValue = maxValue(characteristic);
  }

  private static @Nullable CharacteristicValueStorageKind kind(
      @Nullable CharacteristicValueJdbcColumns characteristic
  ) {
    return characteristic == null ? null : characteristic.kind;
  }

  private static @Nullable BigDecimal exactValue(@Nullable CharacteristicValueJdbcColumns characteristic) {
    return characteristic == null ? null : characteristic.exactValue;
  }

  private static @Nullable BigDecimal minValue(@Nullable CharacteristicValueJdbcColumns characteristic) {
    return characteristic == null ? null : characteristic.minValue;
  }

  private static @Nullable BigDecimal maxValue(@Nullable CharacteristicValueJdbcColumns characteristic) {
    return characteristic == null ? null : characteristic.maxValue;
  }

  private static @Nullable CharacteristicValueJdbcColumns characteristic(
      @Nullable CharacteristicValueStorageKind kind,
      @Nullable BigDecimal exactValue,
      @Nullable BigDecimal minValue,
      @Nullable BigDecimal maxValue
  ) {
    if (kind == null) {
      return null;
    }

    final var characteristic = new CharacteristicValueJdbcColumns();
    characteristic.kind = kind;
    characteristic.exactValue = exactValue;
    characteristic.minValue = minValue;
    characteristic.maxValue = maxValue;
    return characteristic;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
