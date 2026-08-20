package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog;

import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueStorageKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueJdbcMapperSupport
    .toCharacteristic;


/** Maps a catalog row directly to its immutable reference-profile read model. */
final class AppleVarietyReferenceProfileRowMapper {

  private AppleVarietyReferenceProfileRowMapper() {
  }

  static @NonNull AppleVarietyReferenceProfileView toView(
      @NonNull ResultSet resultSet
  ) throws SQLException {
    requireNonNull(resultSet, "apple variety reference profile result set cannot be null");

    final var specificGravity = toCharacteristic(
        characteristic(resultSet, "sugar_kind", "sugar_exact_sg", "sugar_min_sg", "sugar_max_sg"),
        value -> SpecificGravityValue.of(value.toPlainString())
    ).orElse(null); // NOSONAR
    final var titratableAcidity = toCharacteristic(
        characteristic(
            resultSet,
            "ta_kind",
            "ta_exact_g_l_as_malic",
            "ta_min_g_l_as_malic",
            "ta_max_g_l_as_malic"
        ),
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).orElse(null); // NOSONAR
    final var ph = toCharacteristic(
        characteristic(resultSet, "ph_kind", "ph_exact", "ph_min", "ph_max"),
        value -> PhValue.of(value.toPlainString())
    ).orElse(null); // NOSONAR
    final var tannin = toCharacteristic(
        characteristic(
            resultSet,
            "tannin_kind",
            "tannin_exact_mg_l_tae",
            "tannin_min_mg_l_tae",
            "tannin_max_mg_l_tae"
        ),
        value -> TanninValue.of(value.toPlainString())
    ).orElse(null); // NOSONAR

    return new AppleVarietyReferenceProfileView(
        AppleVarietyId.of(resultSet.getObject("variety_id", UUID.class)),
        resultSet.getString("name"),
        specificGravity,
        titratableAcidity,
        ph,
        tannin
    );
  }

  private static @Nullable CharacteristicValueJdbcColumns characteristic(
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

    final var columns = new CharacteristicValueJdbcColumns();
    columns.kind = CharacteristicValueStorageKind.valueOf(kind);
    columns.exactValue = resultSet.getBigDecimal(pointColumn);
    columns.minValue = resultSet.getBigDecimal(lowerColumn);
    columns.maxValue = resultSet.getBigDecimal(upperColumn);
    return columns;
  }
}
