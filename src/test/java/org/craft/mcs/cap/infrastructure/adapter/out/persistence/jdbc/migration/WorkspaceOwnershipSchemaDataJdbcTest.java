package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.migration;

import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresDataJdbcTestConfiguration.class)
class WorkspaceOwnershipSchemaDataJdbcTest {

  @Autowired
  private @NonNull JdbcTemplate jdbcTemplate;

  @Test
  void createsWorkspaceOwnershipAsPartOfTheInitialSchema() {
    final var schema = workspaceOwnershipSchema();

    assertThat(schema)
        .extracting(
            WorkspaceOwnershipSchema::workspaceColumnCount,
            WorkspaceOwnershipSchema::legacyColumnCount,
            WorkspaceOwnershipSchema::workspaceForeignKeyCount
        )
        .containsExactly(2L, 0L, 2L);
  }

  private @NonNull WorkspaceOwnershipSchema workspaceOwnershipSchema() {
    return new WorkspaceOwnershipSchema(
        count("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name IN ('feedstock_batches', 'production_batches')
              AND column_name = 'workspace_id'
            """),
        count("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name IN ('feedstock_batches', 'production_batches', 'workspaces')
              AND column_name = 'cider_maker_id'
            """),
        count("""
            SELECT COUNT(*)
            FROM information_schema.table_constraints
            WHERE table_schema = 'public'
              AND table_name IN ('feedstock_batches', 'production_batches')
              AND constraint_type = 'FOREIGN KEY'
              AND constraint_name IN (
                'fk_feedstock_batches_workspace',
                'fk_production_batches_workspace'
              )
            """)
    );
  }

  private long count(@NonNull String sql) {
    return requireNonNull(jdbcTemplate.queryForObject(sql, Long.class), "count cannot be null");
  }

  private record WorkspaceOwnershipSchema(
      long workspaceColumnCount,
      long legacyColumnCount,
      long workspaceForeignKeyCount
  ) {
  }
}
