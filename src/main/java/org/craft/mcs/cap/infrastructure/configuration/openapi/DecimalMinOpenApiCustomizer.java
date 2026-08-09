package org.craft.mcs.cap.infrastructure.configuration.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * Fixes strict decimal minima for REST record DTO fields where springdoc drops exclusivity.
 *
 * <p>springdoc 3.0.2 may keep {@code minimum: 0} but omit {@code exclusiveMinimum: true} for
 * Java record request properties. Domain rules remain authoritative; this customizer only keeps
 * the generated OpenAPI contract machine-readable for API clients.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
@SuppressWarnings({"rawtypes", "unchecked"})
public final class DecimalMinOpenApiCustomizer
    implements OpenApiLocaleCustomizer {

  private static void applyExclusiveMinimumZero(
      @NonNull Map<@NonNull String, @NonNull Schema> schemas,
      @NonNull String schemaName,
      @NonNull String propertyName
  ) {
    final var schema = schemas.get(schemaName);
    if (schema == null || schema.getProperties() == null) {
      return;
    }
    final var property = property(schema.getProperties(), propertyName);
    if (property == null) {
      return;
    }

    property.minimum(BigDecimal.ZERO);
    property.exclusiveMinimum(true);
    property.exclusiveMinimumValue(BigDecimal.ZERO);
  }

  private static @Nullable Schema<?> property(
      @NonNull Map<String, Schema> properties,
      @NonNull String propertyName
  ) {
    return properties.get(propertyName);
  }

  @Override
  public void customise(
      @NonNull OpenAPI openApi,
      @NonNull Locale locale
  ) {
    final var schemas = openApi.getComponents() == null
                        ? null
                        : openApi.getComponents().getSchemas();
    if (schemas == null || schemas.isEmpty()) {
      return;
    }

    applyExclusiveMinimumZero(schemas, "CreateFeedstockBatchRequest", "availableVolumeLiters");
    applyExclusiveMinimumZero(schemas, "CreateVesselRequest", "nominalCapacityLiters");
    applyExclusiveMinimumZero(schemas, "UpdateVesselRequest", "nominalCapacityLiters");
  }
}
