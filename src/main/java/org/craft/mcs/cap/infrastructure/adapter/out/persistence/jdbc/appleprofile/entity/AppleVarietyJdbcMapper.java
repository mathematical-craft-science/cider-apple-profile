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
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
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
            profile.tannin().orElse(null),
            TanninValue::asBigDecimal
        ),
        appleVariety.chemicalProfileSource() == null
            ? null
            : appleVariety.chemicalProfileSource().value(),
        appleVariety.chemicalProfileConfidence()
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

    entity.phKind = updated.phKind;
    entity.phExactValue = updated.phExactValue;
    entity.phMinValue = updated.phMinValue;
    entity.phMaxValue = updated.phMaxValue;

    entity.titratableAcidityKind = updated.titratableAcidityKind;
    entity.titratableAcidityExactValue = updated.titratableAcidityExactValue;
    entity.titratableAcidityMinValue = updated.titratableAcidityMinValue;
    entity.titratableAcidityMaxValue = updated.titratableAcidityMaxValue;

    entity.tanninKind = updated.tanninKind;
    entity.tanninExactValue = updated.tanninExactValue;
    entity.tanninMinValue = updated.tanninMinValue;
    entity.tanninMaxValue = updated.tanninMaxValue;

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

    entity.phKind = measurementKind(resultSet, "ph_kind");
    entity.phExactValue = resultSet.getBigDecimal("ph_exact");
    entity.phMinValue = resultSet.getBigDecimal("ph_min");
    entity.phMaxValue = resultSet.getBigDecimal("ph_max");

    entity.titratableAcidityKind = measurementKind(resultSet, "ta_kind");
    entity.titratableAcidityExactValue = resultSet.getBigDecimal("ta_exact_g_l_as_malic");
    entity.titratableAcidityMinValue = resultSet.getBigDecimal("ta_min_g_l_as_malic");
    entity.titratableAcidityMaxValue = resultSet.getBigDecimal("ta_max_g_l_as_malic");

    entity.tanninKind = measurementKind(resultSet, "tannin_kind");
    entity.tanninExactValue = resultSet.getBigDecimal("tannin_exact_mg_l_tae");
    entity.tanninMinValue = resultSet.getBigDecimal("tannin_min_mg_l_tae");
    entity.tanninMaxValue = resultSet.getBigDecimal("tannin_max_mg_l_tae");

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
        toChemicalProfile(entity),
        entity.chemicalProfileSource == null
            ? null
            : ChemicalProfileSource.of(entity.chemicalProfileSource),
        entity.chemicalProfileConfidence
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
        entity.tannin(),
        value -> TanninValue.of(value.toPlainString())
    ).ifPresent(builder::tannin);

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

  private static @Nullable Instant instantValue(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getTimestamp(column);
    return value == null ? null : value.toInstant();
  }
}
