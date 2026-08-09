package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.migration;

import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresDataJdbcTestConfiguration.class)
class VesselSchemaDataJdbcTest {

  @Autowired
  private @NonNull JdbcTemplate jdbc;

  @Test
  void usesCanonicalTypesAndWorkspaceScopedVesselReferences() {
    assertThat(columnDefinitions())
        .containsEntry("vessels.vessel_id", "uuid")
        .containsEntry("vessels.workspace_id", "uuid")
        .containsEntry("vessels.nominal_capacity_liters", "numeric(15,3)")
        .containsEntry("vessels.version", "bigint")
        .containsEntry("vessels.created_at", "timestamp with time zone")
        .containsEntry("feedstock_batches.vessel_id", "uuid")
        .containsEntry("production_batches.vessel_id", "uuid");

    assertThat(foreignKeys())
        .containsExactlyInAnyOrder(
            "fk_feedstock_batches_vessel",
            "fk_production_batches_vessel"
        );
  }

  @Test
  void indexesCatalogAndActiveAssignmentsForVesselQueries() {
    assertThat(indexDefinitions())
        .containsEntry(
            "ux_vessels_workspace_lower_name",
            "CREATE UNIQUE INDEX ux_vessels_workspace_lower_name ON public.vessels USING btree "
                + "(workspace_id, lower((name)::text))"
        )
        .containsEntry(
            "idx_vessels_workspace_status",
            "CREATE INDEX idx_vessels_workspace_status ON public.vessels USING btree "
                + "(workspace_id, operational_status)"
        )
        .containsEntry(
            "idx_feedstock_batches_active_workspace_vessel",
            "CREATE INDEX idx_feedstock_batches_active_workspace_vessel ON public.feedstock_batches USING btree "
                + "(workspace_id, vessel_id) WHERE (((lifecycle)::text = 'ACTIVE'::text) "
                + "AND (deleted_at IS NULL))"
        )
        .containsEntry(
            "idx_production_batches_active_workspace_vessel",
            "CREATE INDEX idx_production_batches_active_workspace_vessel ON public.production_batches USING btree "
                + "(workspace_id, vessel_id) WHERE ((lifecycle)::text = 'ACTIVE'::text)"
        );
  }

  private @NonNull Map<@NonNull String, @NonNull String> columnDefinitions() {
    return jdbc.query(
        """
            SELECT table_name,
                   column_name,
                   data_type,
                   numeric_precision,
                   numeric_scale
              FROM information_schema.columns
             WHERE table_schema = 'public'
               AND (table_name, column_name) IN (
                 ('vessels', 'vessel_id'),
                 ('vessels', 'workspace_id'),
                 ('vessels', 'nominal_capacity_liters'),
                 ('vessels', 'version'),
                 ('vessels', 'created_at'),
                 ('feedstock_batches', 'vessel_id'),
                 ('production_batches', 'vessel_id')
               )
            """,
        resultSet -> {
          final var definitions = new java.util.LinkedHashMap<String, String>();
          while (resultSet.next()) {
            final var key = resultSet.getString("table_name") + "." + resultSet.getString("column_name");
            final var dataType = resultSet.getString("data_type");
            definitions.put(
                key,
                "numeric".equals(dataType)
                    ? "numeric(%d,%d)".formatted(
                        resultSet.getInt("numeric_precision"),
                        resultSet.getInt("numeric_scale")
                    )
                    : dataType
            );
          }
          return Map.copyOf(definitions);
        }
    );
  }

  private @NonNull List<@NonNull String> foreignKeys() {
    return jdbc.queryForList(
        """
            SELECT constraint_name
              FROM information_schema.table_constraints
             WHERE table_schema = 'public'
               AND table_name IN ('feedstock_batches', 'production_batches')
               AND constraint_type = 'FOREIGN KEY'
               AND constraint_name IN ('fk_feedstock_batches_vessel', 'fk_production_batches_vessel')
            """,
        String.class
    );
  }

  private @NonNull Map<@NonNull String, @NonNull String> indexDefinitions() {
    return jdbc.query(
        """
            SELECT indexname, indexdef
              FROM pg_indexes
             WHERE schemaname = 'public'
               AND indexname IN (
                 'ux_vessels_workspace_lower_name',
                 'idx_vessels_workspace_status',
                 'idx_feedstock_batches_active_workspace_vessel',
                 'idx_production_batches_active_workspace_vessel'
               )
            """,
        resultSet -> {
          final var definitions = new java.util.LinkedHashMap<String, String>();
          while (resultSet.next()) {
            definitions.put(resultSet.getString("indexname"), resultSet.getString("indexdef"));
          }
          return Map.copyOf(definitions);
        }
    );
  }
}
