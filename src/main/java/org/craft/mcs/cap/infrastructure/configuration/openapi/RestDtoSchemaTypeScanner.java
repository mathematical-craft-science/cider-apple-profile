package org.craft.mcs.cap.infrastructure.configuration.openapi;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import static java.util.Objects.requireNonNull;


/** Discovers REST record DTOs that must be registered as reusable OpenAPI schemas. */
@Component
public final class RestDtoSchemaTypeScanner {

  private static final @NonNull List<@NonNull String> REST_DTO_RESOURCE_PATTERNS = List.of(
      "classpath*:org/craft/mcs/cap/infrastructure/adapter/in/rest/**/dto/*.class",
      "classpath*:org/craft/mcs/cap/shared/infrastructure/rest/**/dto/*.class"
  );

  private final @NonNull ResourceLoader resourceLoader;

  public RestDtoSchemaTypeScanner(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = requireNonNull(resourceLoader, "resource loader cannot be null");
  }

  public @NonNull Set<@NonNull Class<?>> recordDtoTypes() {
    final var loader = requireNonNull(resourceLoader.getClassLoader(), "resource class loader cannot be null");
    final var resolver = new PathMatchingResourcePatternResolver(resourceLoader);
    final var metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    final var dtoTypes = new LinkedHashSet<Class<?>>();

    try {
      for (final var resourcePattern : REST_DTO_RESOURCE_PATTERNS) {
        for (Resource resource : resolver.getResources(resourcePattern)) {
          final var className = metadataReaderFactory
              .getMetadataReader(resource)
              .getClassMetadata()
              .getClassName();
          final var dtoType = ClassUtils.forName(className, loader);
          if (dtoType.isRecord()) {
            dtoTypes.add(dtoType);
          }
        }
      }
    } catch (ReflectiveOperationException | IOException exception) {
      throw new IllegalStateException("cannot scan REST DTO classes for OpenAPI schema customizers", exception);
    }

    return dtoTypes;
  }
}
