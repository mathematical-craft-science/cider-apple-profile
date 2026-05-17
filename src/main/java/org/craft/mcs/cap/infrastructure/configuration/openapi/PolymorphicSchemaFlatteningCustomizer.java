package org.craft.mcs.cap.infrastructure.configuration.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public final class PolymorphicSchemaFlatteningCustomizer implements OpenApiCustomizer {

  private final @NonNull RestDtoSchemaTypeScanner dtoSchemaTypeScanner;

  public PolymorphicSchemaFlatteningCustomizer(@NonNull RestDtoSchemaTypeScanner dtoSchemaTypeScanner) {
    this.dtoSchemaTypeScanner = requireNonNull(dtoSchemaTypeScanner, "DTO schema type scanner cannot be null");
  }

  @Override
  public void customise(@NonNull OpenAPI openApi) {
    final var components = openApi.getComponents();
    if (components == null || components.getSchemas() == null) {
      return;
    }

    dtoSchemaTypeScanner.recordDtoTypes()
        .forEach(dtoType -> flattenPolymorphicSubtypeSchema(components, dtoType));
  }

  @SuppressWarnings("unchecked")
  private static void flattenPolymorphicSubtypeSchema(
      @NonNull Components components,
      @NonNull Class<?> dtoType
  ) {
    if (dtoType.getInterfaces().length == 0) {
      return;
    }

    final var schema = components.getSchemas().get(dtoType.getSimpleName());
    if (schema == null || schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
      return;
    }

    final var mergedProperties = new LinkedHashMap<String, Schema<?>>();
    final var required = new ArrayList<String>();

    for (Schema<?> part : allOfSchemas(schema)) {
      mergeSchemaPart(components, part, mergedProperties, required);
    }

    schema.setAllOf(null);
    schema.setType("object");
    schema.setTypes(Set.of("object"));
    schema.setProperties(mergedProperties);
    if (!required.isEmpty()) {
      schema.setRequired(required);
    }
  }

  private static void mergeSchemaPart(
      @NonNull Components components,
      @NonNull Schema<?> part,
      @NonNull Map<@NonNull String, @NonNull Schema<?>> targetProperties,
      @NonNull List<@NonNull String> targetRequired
  ) {
    final var resolved = resolveSchemaPart(components, part);
    if (resolved == null) {
      return;
    }

    if (resolved.getProperties() != null) {
      schemaProperties(resolved).forEach(targetProperties::putIfAbsent);
    }
    if (resolved.getRequired() != null) {
      resolved.getRequired().stream()
          .filter(required -> !targetRequired.contains(required))
          .forEach(targetRequired::add);
    }
  }

  private static @Nullable Schema<?> resolveSchemaPart(
      @NonNull Components components,
      @NonNull Schema<?> part
  ) {
    final var ref = part.get$ref();
    if (ref == null || ref.isBlank()) {
      return part;
    }

    final var schemaName = ref.substring(ref.lastIndexOf('/') + 1);
    return components.getSchemas().get(schemaName);
  }

  @SuppressWarnings("unchecked")
  private static @NonNull Map<@NonNull String, @NonNull Schema<?>> schemaProperties(@NonNull Schema<?> schema) {
    return (Map<String, Schema<?>>) (Map<?, ?>) schema.getProperties();
  }

  @SuppressWarnings("unchecked")
  private static @NonNull List<@NonNull Schema<?>> allOfSchemas(@NonNull Schema<?> schema) {
    return (List<Schema<?>>) (List<?>) schema.getAllOf();
  }
}
