package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.toCharacteristic;


public final class FeedstockMeasurementJdbcMapper {

  private FeedstockMeasurementJdbcMapper() {
  }

  public static @NonNull FeedstockMeasurementJdbcEntity fromDomain(@NonNull FeedstockMeasurement sample) {
    requireNonNull(sample, "feedstock measurement cannot be null");

    final var profile = sample.chemicalProfile();

    return new FeedstockMeasurementJdbcEntity(
        sample.measurementId().value(),
        sample.batchId().value(),
        sample.name().value(),
        sample.measuredAt(),
        sample.note(),
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

  public static @NonNull FeedstockMeasurementJdbcEntity fromResultSet(@NonNull ResultSet resultSet)
      throws SQLException {
    requireNonNull(resultSet, "feedstock measurement result set cannot be null");

    final var entity = new FeedstockMeasurementJdbcEntity();
    entity.measurementId = resultSet.getObject("measurement_id", java.util.UUID.class);
    entity.batch = AggregateReference.to(resultSet.getObject("batch_id", java.util.UUID.class));
    entity.name = resultSet.getString("name");
    entity.measuredAt = resultSet.getTimestamp("measured_at").toInstant();
    entity.note = resultSet.getString("note");

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
    entity.createdAt =
        resultSet.getTimestamp("created_at") == null ? null : resultSet.getTimestamp("created_at").toInstant();
    entity.updatedAt =
        resultSet.getTimestamp("updated_at") == null ? null : resultSet.getTimestamp("updated_at").toInstant();
    entity.version = resultSet.getLong("version");
    return entity;
  }

  public static @NonNull FeedstockMeasurement toDomain(@NonNull FeedstockMeasurementJdbcEntity entity) {
    requireNonNull(entity, "feedstock measurement row cannot be null");

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

    return FeedstockMeasurement.create(
        FeedstockMeasurementId.of(requireNonNull(entity.measurementId, "feedstock measurement id cannot be null")),
        FeedstockBatchId.of(requireNonNull(entity.batch, "feedstock batch reference cannot be null").getId()),
        FeedstockMeasurementName.of(requireNonNull(entity.name, "feedstock measurement name cannot be null")),
        builder.build(),
        requireNonNull(entity.measuredAt, "measured at cannot be null")
    ).changeNote(entity.note);
  }

  @Nullable
  private static MeasurementKind measurementKind(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getString(column);
    return value == null
           ? null
           : MeasurementKind.valueOf(value);
  }

  @Nullable
  private static ConfidenceLevel confidenceLevel(
      @NonNull ResultSet resultSet,
      @NonNull String column // NOSONAR
  ) throws SQLException {
    final var value = resultSet.getString(column);
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  @Nullable
  private static Integer integerValue(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    final var value = resultSet.getInt(column);
    return resultSet.wasNull() ? null : value;
  }
}
