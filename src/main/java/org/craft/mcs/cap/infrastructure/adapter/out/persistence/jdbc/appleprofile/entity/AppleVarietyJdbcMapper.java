package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.toCharacteristic;

public final class AppleVarietyJdbcMapper {

  private AppleVarietyJdbcMapper() {
  }

  public static @NonNull AppleVarietyJdbcEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.chemicalProfile();

    return new AppleVarietyJdbcEntity(
        appleVariety.varietyId().value(),
        appleVariety.name().value(),
        fromNullableCharacteristic(
            profile.sugar().orElse(null),
            sugar -> sugar.specificGravity().asBigDecimal()
        ),
        fromNullableCharacteristic(
            profile.ph().orElse(null),
            PhValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.titratableAcidity().orElse(null),
            TitratableAcidityValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.tanninity().orElse(null),
            TanninityValue::asBigDecimal
        ),
        profile.source() == null ? null : profile.source().value(),
        profile.confidence()
    );
  }

  public static @NonNull AppleVarietyJdbcEntity updateFromDomain(
      @NonNull AppleVariety appleVariety,
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    requireNonNull(appleVariety, "apple variety cannot be null");
    requireNonNull(entity, "apple variety row cannot be null");

    final var updated = fromDomain(appleVariety);

    entity.name = updated.name;

    entity.sugarKind = updated.sugarKind;
    entity.sugarExactValue = updated.sugarExactValue;
    entity.sugarMinValue = updated.sugarMinValue;
    entity.sugarMaxValue = updated.sugarMaxValue;
    entity.sugarMeanValue = updated.sugarMeanValue;
    entity.sugarMedianValue = updated.sugarMedianValue;
    entity.sugarStandardDeviation = updated.sugarStandardDeviation;
    entity.sugarSampleSize = updated.sugarSampleSize;

    entity.phKind = updated.phKind;
    entity.phExactValue = updated.phExactValue;
    entity.phMinValue = updated.phMinValue;
    entity.phMaxValue = updated.phMaxValue;
    entity.phMeanValue = updated.phMeanValue;
    entity.phMedianValue = updated.phMedianValue;
    entity.phStandardDeviation = updated.phStandardDeviation;
    entity.phSampleSize = updated.phSampleSize;

    entity.titratableAcidityKind = updated.titratableAcidityKind;
    entity.titratableAcidityExactValue = updated.titratableAcidityExactValue;
    entity.titratableAcidityMinValue = updated.titratableAcidityMinValue;
    entity.titratableAcidityMaxValue = updated.titratableAcidityMaxValue;
    entity.titratableAcidityMeanValue = updated.titratableAcidityMeanValue;
    entity.titratableAcidityMedianValue = updated.titratableAcidityMedianValue;
    entity.titratableAcidityStandardDeviation = updated.titratableAcidityStandardDeviation;
    entity.titratableAciditySampleSize = updated.titratableAciditySampleSize;

    entity.tanninityKind = updated.tanninityKind;
    entity.tanninityExactValue = updated.tanninityExactValue;
    entity.tanninityMinValue = updated.tanninityMinValue;
    entity.tanninityMaxValue = updated.tanninityMaxValue;
    entity.tanninityMeanValue = updated.tanninityMeanValue;
    entity.tanninityMedianValue = updated.tanninityMedianValue;
    entity.tanninityStandardDeviation = updated.tanninityStandardDeviation;
    entity.tanninitySampleSize = updated.tanninitySampleSize;

    entity.chemicalProfileSource = updated.chemicalProfileSource;
    entity.chemicalProfileConfidence = updated.chemicalProfileConfidence;

    return entity;
  }

  public static @NonNull AppleVarietyJdbcEntity fromResultSet(@NonNull ResultSet resultSet)
      throws SQLException {
    requireNonNull(resultSet, "apple variety result set cannot be null");

    final var entity = new AppleVarietyJdbcEntity();
    entity.varietyId = resultSet.getObject("variety_id", UUID.class);
    entity.name = resultSet.getString("name");

    entity.sugarKind = measurementKind(resultSet, "sugar_kind");
    entity.sugarExactValue = resultSet.getBigDecimal("sugar_exact_sg");
    entity.sugarMinValue = resultSet.getBigDecimal("sugar_min_sg");
    entity.sugarMaxValue = resultSet.getBigDecimal("sugar_max_sg");
    entity.sugarMeanValue = resultSet.getBigDecimal("sugar_mean_sg");
    entity.sugarMedianValue = resultSet.getBigDecimal("sugar_median_sg");
    entity.sugarStandardDeviation = resultSet.getBigDecimal("sugar_standard_deviation");
    entity.sugarSampleSize = integerValue(resultSet, "sugar_sample_size");

    entity.phKind = measurementKind(resultSet, "ph_kind");
    entity.phExactValue = resultSet.getBigDecimal("ph_exact");
    entity.phMinValue = resultSet.getBigDecimal("ph_min");
    entity.phMaxValue = resultSet.getBigDecimal("ph_max");
    entity.phMeanValue = resultSet.getBigDecimal("ph_mean");
    entity.phMedianValue = resultSet.getBigDecimal("ph_median");
    entity.phStandardDeviation = resultSet.getBigDecimal("ph_standard_deviation");
    entity.phSampleSize = integerValue(resultSet, "ph_sample_size");

    entity.titratableAcidityKind = measurementKind(resultSet, "ta_kind");
    entity.titratableAcidityExactValue = resultSet.getBigDecimal("ta_exact_g_l_as_malic");
    entity.titratableAcidityMinValue = resultSet.getBigDecimal("ta_min_g_l_as_malic");
    entity.titratableAcidityMaxValue = resultSet.getBigDecimal("ta_max_g_l_as_malic");
    entity.titratableAcidityMeanValue = resultSet.getBigDecimal("ta_mean_g_l_as_malic");
    entity.titratableAcidityMedianValue = resultSet.getBigDecimal("ta_median_g_l_as_malic");
    entity.titratableAcidityStandardDeviation = resultSet.getBigDecimal("ta_standard_deviation");
    entity.titratableAciditySampleSize = integerValue(resultSet, "ta_sample_size");

    entity.tanninityKind = measurementKind(resultSet, "tanninity_kind");
    entity.tanninityExactValue = resultSet.getBigDecimal("tanninity_exact_mg_l_tae");
    entity.tanninityMinValue = resultSet.getBigDecimal("tanninity_min_mg_l_tae");
    entity.tanninityMaxValue = resultSet.getBigDecimal("tanninity_max_mg_l_tae");
    entity.tanninityMeanValue = resultSet.getBigDecimal("tanninity_mean_mg_l_tae");
    entity.tanninityMedianValue = resultSet.getBigDecimal("tanninity_median_mg_l_tae");
    entity.tanninityStandardDeviation = resultSet.getBigDecimal("tanninity_standard_deviation");
    entity.tanninitySampleSize = integerValue(resultSet, "tanninity_sample_size");

    entity.chemicalProfileSource = resultSet.getString("chemical_profile_source");
    entity.chemicalProfileConfidence = confidenceLevel(resultSet, "chemical_profile_confidence");
    entity.createdAt = instantValue(resultSet, "created_at");
    entity.updatedAt = instantValue(resultSet, "updated_at");
    entity.version = resultSet.getLong("version");
    return entity;
  }

  public static @NonNull AppleVariety toDomain(@NonNull AppleVarietyJdbcEntity entity) {
    requireNonNull(entity, "apple variety row cannot be null");

    return AppleVariety.create(
        AppleVarietyId.of(entity.varietyId),
        AppleVarietyName.of(entity.name),
        toChemicalProfile(entity)
    );
  }

  public static @NonNull VersionedAggregate<AppleVariety> toVersionedAggregate(
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  public static long revision(@NonNull AppleVarietyJdbcEntity entity) {
    return requireNonNull(entity, "apple variety row cannot be null").version;
  }

  public static @NonNull AppleVarietySugarAcidityPointCandidateView toCandidateView(
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    requireNonNull(entity, "apple variety row cannot be null");

    return new AppleVarietySugarAcidityPointCandidateView(
        AppleVarietyId.of(entity.varietyId),
        AppleVarietyName.of(entity.name),
        toChemicalProfile(entity)
    );
  }

  private static @NonNull ChemicalProfile toChemicalProfile(@NonNull AppleVarietyJdbcEntity entity) {
    requireNonNull(entity, "apple variety row cannot be null");

    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        entity.sugar(),
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);

    toCharacteristic(
        entity.ph(),
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);

    toCharacteristic(
        entity.titratableAcidity(),
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);

    toCharacteristic(
        entity.tanninity(),
        value -> TanninityValue.of(value.toPlainString())
    ).ifPresent(builder::tanninity);

    if (entity.chemicalProfileSource != null) {
      builder.source(ChemicalProfileSource.of(entity.chemicalProfileSource));
    }

    builder.confidence(entity.chemicalProfileConfidence);

    return builder.build();
  }

  private static @Nullable MeasurementKind measurementKind(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getString(column);
    return value == null ? null : MeasurementKind.valueOf(value);
  }

  private static @Nullable ConfidenceLevel confidenceLevel(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getString(column);
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  private static @Nullable Integer integerValue(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getInt(column);
    return resultSet.wasNull() ? null : value;
  }

  private static @Nullable Instant instantValue(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getTimestamp(column);
    return value == null ? null : value.toInstant();
  }
}
