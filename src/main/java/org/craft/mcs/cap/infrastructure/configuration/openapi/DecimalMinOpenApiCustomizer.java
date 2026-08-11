package org.craft.mcs.cap.infrastructure.configuration.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * Fixes strict decimal minima for REST DTO fields and query parameters where springdoc drops exclusivity.
 *
 * <p>springdoc 3.0.2 may keep {@code minimum: 0} but omit {@code exclusiveMinimum: true} for
 * request properties and parameters. Domain rules remain authoritative; this customizer only keeps
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

    applyExclusiveMinimumZero(property);
  }

  private static void applyParameterExclusiveMinimumZero(
      @NonNull OpenAPI openApi,
      @NonNull String path,
      @NonNull String parameterName
  ) {
    final var paths = openApi.getPaths();
    final var pathItem = paths == null ? null : paths.get(path);
    final var operation = pathItem == null ? null : pathItem.getGet();
    if (operation == null || operation.getParameters() == null) {
      return;
    }
    operation.getParameters().stream()
        .filter(parameter -> parameterName.equals(parameter.getName()))
        .map(Parameter::getSchema)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(DecimalMinOpenApiCustomizer::applyExclusiveMinimumZero);
  }

  private static void applyExclusiveMinimumZero(@NonNull Schema<?> schema) {
    schema.minimum(BigDecimal.ZERO);
    schema.exclusiveMinimum(true);
    schema.exclusiveMinimumValue(BigDecimal.ZERO);
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
    applyParameterExclusiveMinimumZero(
        openApi,
        "/vessels/available",
        "requiredVolumeLiters"
    );
  }
}
