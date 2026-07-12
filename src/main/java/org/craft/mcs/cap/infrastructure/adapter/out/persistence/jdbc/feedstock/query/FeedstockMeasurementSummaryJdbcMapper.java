package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.toCharacteristic;


/**
 * Maps read-side measurement summary rows without rehydrating the FeedstockMeasurement aggregate.
 */
final class FeedstockMeasurementSummaryJdbcMapper {

  private FeedstockMeasurementSummaryJdbcMapper() {
  }

  static @NonNull FeedstockMeasurementSummary fromResultSet(@NonNull ResultSet resultSet)
      throws SQLException {
    requireNonNull(resultSet, "feedstock measurement result set cannot be null");

    return new FeedstockMeasurementSummary(
        FeedstockMeasurementId.of(resultSet.getObject("measurement_id", UUID.class)),
        CiderMakerId.of(resultSet.getObject("cider_maker_id", UUID.class)),
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        FeedstockMeasurementName.of(resultSet.getString("name")),
        requireNonNull(resultSet.getTimestamp("measured_at"), "measured at cannot be null").toInstant(),
        chemicalProfile(resultSet),
        resultSet.getString("note")
    );
  }

  private static @NonNull ChemicalProfile chemicalProfile(@NonNull ResultSet resultSet)
      throws SQLException {
    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        measurement(resultSet, "sugar"),
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);

    toCharacteristic(
        measurement(resultSet, "ph"),
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);

    toCharacteristic(
        measurement(resultSet, "ta"),
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);

    toCharacteristic(
        measurement(resultSet, "tanninity"),
        value -> TanninityValue.of(value.toPlainString())
    ).ifPresent(builder::tanninity);

    toCharacteristic(
        measurement(resultSet, "yan"),
        value -> YeastAssimilableNitrogenValue.of(value.toPlainString())
    ).ifPresent(builder::yeastAssimilableNitrogen);

    final var source = resultSet.getString("chemical_profile_source");
    if (source != null) {
      builder.source(ChemicalProfileSource.of(source));
    }

    builder.confidence(confidenceLevel(resultSet, "chemical_profile_confidence"));
    return builder.build();
  }

  private static @Nullable MeasurementJdbcColumns measurement(
      @NonNull ResultSet resultSet,
      @NonNull String prefix
  ) throws SQLException {
    final var kind = measurementKind(resultSet, prefix + "_kind");
    if (kind == null) {
      return null;
    }

    final var columns = new MeasurementJdbcColumns();
    columns.kind = kind;
    columns.exactValue = resultSet.getBigDecimal(exactValueColumn(prefix));
    columns.minValue = resultSet.getBigDecimal(minValueColumn(prefix));
    columns.maxValue = resultSet.getBigDecimal(maxValueColumn(prefix));
    columns.meanValue = resultSet.getBigDecimal(meanValueColumn(prefix));
    columns.medianValue = resultSet.getBigDecimal(medianValueColumn(prefix));
    columns.standardDeviation = resultSet.getBigDecimal(prefix + "_standard_deviation");
    columns.sampleSize = integerValue(resultSet, prefix + "_sample_size");
    return columns;
  }

  private static @NonNull String exactValueColumn(@NonNull String prefix) {
    return switch (prefix) {
      case "sugar" -> "sugar_exact_sg";
      case "ph" -> "ph_exact";
      case "ta" -> "ta_exact_g_l_as_malic";
      case "tanninity" -> "tanninity_exact_mg_l_tae";
      case "yan" -> "yan_exact_mg_n_l";
      default -> throw new IllegalArgumentException("unsupported measurement column prefix: " + prefix);
    };
  }

  private static @NonNull String minValueColumn(@NonNull String prefix) {
    return switch (prefix) {
      case "sugar" -> "sugar_min_sg";
      case "ph" -> "ph_min";
      case "ta" -> "ta_min_g_l_as_malic";
      case "tanninity" -> "tanninity_min_mg_l_tae";
      case "yan" -> "yan_min_mg_n_l";
      default -> throw new IllegalArgumentException("unsupported measurement column prefix: " + prefix);
    };
  }

  private static @NonNull String maxValueColumn(@NonNull String prefix) {
    return switch (prefix) {
      case "sugar" -> "sugar_max_sg";
      case "ph" -> "ph_max";
      case "ta" -> "ta_max_g_l_as_malic";
      case "tanninity" -> "tanninity_max_mg_l_tae";
      case "yan" -> "yan_max_mg_n_l";
      default -> throw new IllegalArgumentException("unsupported measurement column prefix: " + prefix);
    };
  }

  private static @NonNull String meanValueColumn(@NonNull String prefix) {
    return switch (prefix) {
      case "sugar" -> "sugar_mean_sg";
      case "ph" -> "ph_mean";
      case "ta" -> "ta_mean_g_l_as_malic";
      case "tanninity" -> "tanninity_mean_mg_l_tae";
      case "yan" -> "yan_mean_mg_n_l";
      default -> throw new IllegalArgumentException("unsupported measurement column prefix: " + prefix);
    };
  }

  private static @NonNull String medianValueColumn(@NonNull String prefix) {
    return switch (prefix) {
      case "sugar" -> "sugar_median_sg";
      case "ph" -> "ph_median";
      case "ta" -> "ta_median_g_l_as_malic";
      case "tanninity" -> "tanninity_median_mg_l_tae";
      case "yan" -> "yan_median_mg_n_l";
      default -> throw new IllegalArgumentException("unsupported measurement column prefix: " + prefix);
    };
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
      @NonNull String column
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
