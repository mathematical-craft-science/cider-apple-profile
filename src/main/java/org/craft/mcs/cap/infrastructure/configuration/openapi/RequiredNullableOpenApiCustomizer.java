package org.craft.mcs.cap.infrastructure.configuration.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class RequiredNullableOpenApiCustomizer implements OpenApiCustomizer {

  private final @NonNull RestDtoSchemaTypeScanner dtoSchemaTypeScanner;

  public RequiredNullableOpenApiCustomizer(@NonNull RestDtoSchemaTypeScanner dtoSchemaTypeScanner) {
    this.dtoSchemaTypeScanner = requireNonNull(dtoSchemaTypeScanner, "DTO schema type scanner cannot be null");
  }

  @Override
  public void customise(@NonNull OpenAPI openApi) {
    final var schemas = openApi.getComponents() == null
        ? null
        : openApi.getComponents().getSchemas();
    if (schemas == null || schemas.isEmpty()) {
      return;
    }

    dtoSchemaTypeScanner.recordDtoTypes().forEach(dtoClass -> applyNullness(schemas, dtoClass));
  }

  private static void applyNullness(
      @NonNull Map<@NonNull String, @NonNull Schema> schemas,
      @NonNull Class<?> dtoClass
  ) {
    final var schema = schemas.get(dtoClass.getSimpleName());
    if (schema == null || schema.getProperties() == null) {  // NOSONAR
      return;
    }

    for (final var component : dtoClass.getRecordComponents()) {
      final var property = property(schema, component);
      if (property == null) {
        continue;
      }

      if (isNullable(component)) {
        markNullable(property);
      } else if (isRequired(component)) {
        addRequiredProperty(schema, component.getName());
      }
    }
  }

  private static @Nullable Schema<?> property(
      @NonNull Schema<?> schema,
      @NonNull RecordComponent component
  ) {
    return schema.getProperties().get(component.getName());
  }

  private static boolean isRequired(@NonNull RecordComponent component) {
    return component.getType().isPrimitive()
        || hasAnnotation(component, NonNull.class);
  }

  private static boolean isNullable(@NonNull RecordComponent component) {
    return hasAnnotation(component, Nullable.class);
  }

  private static boolean hasAnnotation(
      @NonNull RecordComponent component,
      @NonNull Class<? extends Annotation> annotationType
  ) {
    return component.isAnnotationPresent(annotationType)
        || component.getAnnotatedType().isAnnotationPresent(annotationType)
        || component.getAccessor().isAnnotationPresent(annotationType)
        || component.getAccessor().getAnnotatedReturnType().isAnnotationPresent(annotationType);
  }

  private static void addRequiredProperty(
      @NonNull Schema<?> schema,
      @NonNull String propertyName
  ) {
    final var required = schema.getRequired() == null
        ? new LinkedHashSet<String>()
        : new LinkedHashSet<>(schema.getRequired());

    if (required.add(propertyName)) {
      schema.setRequired(required.stream().toList());
    }
  }

  private static void markNullable(@NonNull Schema<?> property) {
    property.nullable(true);
    if (property.get$ref() != null) {
      final var ref = property.get$ref();
      property.set$ref(null);
      property.setAnyOf(List.of(
          new Schema<>().$ref(ref),
          new Schema<>().types(new LinkedHashSet<>(List.of("null")))
      ));
      return;
    }

    final var types = property.getTypes() == null
        ? new LinkedHashSet<String>()
        : new LinkedHashSet<>(property.getTypes());
    if (property.getType() != null) {
      types.add(property.getType());
      property.setType(null);
    }
    types.add("null");
    property.setTypes(types);
  }
}
