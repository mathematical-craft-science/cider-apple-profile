package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * JDBC read-side adapter for production batch views.
 */
@Repository
@RequiredArgsConstructor
public class JdbcProductionBatchQueryRepository implements ProductionBatchQueryRepository {

  private static final @NonNull String SELECT_VIEW = """
      SELECT batch_id,
             cider_maker_id,
             name,
             kind,
             status,
             total_volume_liters,
             sugar_specific_gravity,
             acidity_g_l_as_malic,
             tannin_mg_l_tae,
             yan_mg_n_l,
             target_distance,
             note
      FROM production_batches
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Optional<@NonNull ProductionBatchView> findViewById(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");

    final var views = jdbcTemplate.query(
        SELECT_VIEW + "WHERE batch_id = :batchId",
        Map.of("batchId", batchId.value()),
        JdbcProductionBatchQueryRepository::toView
    );
    return views.stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull ProductionBatchView> findViewsByCiderMakerId(
      @NonNull CiderMakerId ciderMakerId
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return jdbcTemplate.query(
        SELECT_VIEW + """
            WHERE cider_maker_id = :ciderMakerId
            ORDER BY created_at DESC, batch_id DESC
            """,
        Map.of("ciderMakerId", ciderMakerId.value()),
        JdbcProductionBatchQueryRepository::toView
    );
  }

  private static @NonNull ProductionBatchView toView(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new ProductionBatchView(
        ProductionBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        CiderMakerId.of(resultSet.getObject("cider_maker_id", UUID.class)),
        ProductionBatchName.of(resultSet.getString("name")),
        ProductionBatchKind.valueOf(resultSet.getString("kind")),
        ProductionBatchStatus.valueOf(resultSet.getString("status")),
        VolumeValue.ofLiters(resultSet.getBigDecimal("total_volume_liters")),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of(resultSet.getBigDecimal("sugar_specific_gravity")),
            TitratableAcidityValue.of(resultSet.getBigDecimal("acidity_g_l_as_malic")),
            tannin(resultSet),
            yeastAssimilableNitrogen(resultSet)
        ),
        targetDistance(resultSet),
        resultSet.getString("note")
    );
  }

  private static @Nullable TanninityValue tannin(@NonNull ResultSet resultSet)
      throws SQLException {
    final var value = resultSet.getBigDecimal("tannin_mg_l_tae");
    return value == null ? null : TanninityValue.of(value);
  }

  private static @Nullable YeastAssimilableNitrogenValue yeastAssimilableNitrogen(
      @NonNull ResultSet resultSet
  ) throws SQLException {
    final var value = resultSet.getBigDecimal("yan_mg_n_l");
    return value == null ? null : YeastAssimilableNitrogenValue.of(value);
  }

  private static @Nullable ProductionTargetDistance targetDistance(@NonNull ResultSet resultSet)
      throws SQLException {
    final var value = resultSet.getBigDecimal("target_distance");
    return value == null ? null : ProductionTargetDistance.of(value);
  }
}
