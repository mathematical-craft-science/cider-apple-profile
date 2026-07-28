package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport
    .toCharacteristic;


/** Maps the AppleVariety analysis query row directly to its application read model. */
final class AppleVarietyAnalysisCandidateRowMapper {

  private AppleVarietyAnalysisCandidateRowMapper() {
  }

  static @NonNull AppleVarietyAnalysisCandidateView toView(
      @NonNull ResultSet resultSet
  ) throws SQLException {
    requireNonNull(resultSet, "apple variety analysis candidate result set cannot be null");

    return new AppleVarietyAnalysisCandidateView(
        AppleVarietyId.of(resultSet.getObject("variety_id", UUID.class)),
        AppleVarietyName.of(resultSet.getString("name")),
        toChemicalProfile(resultSet)
    );
  }

  private static @NonNull ChemicalProfile toChemicalProfile(
      @NonNull ResultSet resultSet
  ) throws SQLException {
    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        measurement(resultSet, "sugar_kind", "sugar_exact_sg", "sugar_min_sg", "sugar_max_sg"),
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);
    toCharacteristic(
        measurement(resultSet, "ph_kind", "ph_exact", "ph_min", "ph_max"),
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);
    toCharacteristic(
        measurement(resultSet, "ta_kind", "ta_exact_g_l_as_malic", "ta_min_g_l_as_malic",
            "ta_max_g_l_as_malic"),
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);
    toCharacteristic(
        measurement(resultSet, "tannin_kind", "tannin_exact_mg_l_tae", "tannin_min_mg_l_tae",
            "tannin_max_mg_l_tae"),
        value -> TanninValue.of(value.toPlainString())
    ).ifPresent(builder::tannin);

    return builder.build();
  }

  private static @Nullable MeasurementJdbcColumns measurement(
      @NonNull ResultSet resultSet,
      @NonNull String kindColumn,
      @NonNull String pointColumn,
      @NonNull String lowerColumn,
      @NonNull String upperColumn
  ) throws SQLException {
    final var kind = resultSet.getString(kindColumn);
    if (kind == null) {
      return null;
    }

    final var columns = new MeasurementJdbcColumns();
    columns.kind = MeasurementKind.valueOf(kind);
    columns.exactValue = resultSet.getBigDecimal(pointColumn);
    columns.minValue = resultSet.getBigDecimal(lowerColumn);
    columns.maxValue = resultSet.getBigDecimal(upperColumn);
    return columns;
  }
}
