package org.craft.mcs.cap.shared.platform.persistence.jdbc;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static java.util.Objects.requireNonNull;


/**
 * Small JDBC query builder for adapter-level read models with optional filters.
 * It keeps dynamic SQL assembly explicit while avoiding sentinel parameters and
 * boolean flags in SQL predicates.
 */
public final class SqlQueryBuilder {

  private final @NonNull StringBuilder sql;
  private final @NonNull MapSqlParameterSource parameters = new MapSqlParameterSource();

  private SqlQueryBuilder(@NonNull String baseSql) {
    sql = new StringBuilder(requireNonNull(baseSql, "base sql cannot be null"));
  }

  public static @NonNull SqlQueryBuilder from(@NonNull String baseSql) {
    return new SqlQueryBuilder(baseSql);
  }

  public @NonNull SqlQueryBuilder with(@NonNull SqlQueryOption option) {
    requireNonNull(option, "sql query option cannot be null").apply(this);
    return this;
  }

  public @NonNull SqlQueryBuilder where(@NonNull String condition) {
    sql.append("  and (")
        .append(requireNonNull(condition, "sql condition cannot be null").strip())
        .append(")\n");
    return this;
  }

  public @NonNull SqlQueryBuilder parameter(@NonNull String name, @Nullable Object value) {
    parameters.addValue(requireNonNull(name, "sql parameter name cannot be null"), value);
    return this;
  }

  public @NonNull SqlQueryBuilder orderBy(@NonNull String clause) {
    sql.append("order by ")
        .append(requireNonNull(clause, "sql order by clause cannot be null").strip())
        .append('\n');
    return this;
  }

  public @NonNull SqlQueryBuilder limit(int limit) {
    sql.append("limit :limit\n");
    parameters.addValue("limit", limit);
    return this;
  }

  public @NonNull BuiltSqlQuery build() {
    return new BuiltSqlQuery(sql.toString(), parameters);
  }

  @FunctionalInterface
  public interface SqlQueryOption {

    void apply(@NonNull SqlQueryBuilder builder);
  }

  public record BuiltSqlQuery(
      @NonNull String sql,
      @NonNull SqlParameterSource parameters
  ) {
  }
}
