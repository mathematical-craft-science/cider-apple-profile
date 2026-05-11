package org.craft.mcs.cap.architecture;


import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


class PackageBoundaryTest {

  private static final @NonNull Path MAIN_SOURCES = Path.of("src/main/java");
  private static final @NonNull Path MAIN_RESOURCES = Path.of("src/main/resources");
  private static final @NonNull String DEPRECATED_SHARED_DOMAIN_PACKAGE =
      "org.craft.mcs.cap.shared." + "domain";

  @Test
  void sharedDomainPackageIsNotUsedAnymore() throws IOException {
    assertNoTextInMainSources(
        DEPRECATED_SHARED_DOMAIN_PACKAGE,
        "deprecated shared domain has been split into shared.kernel and shared.platform"
    );
    assertNoTextInMainResources(
        DEPRECATED_SHARED_DOMAIN_PACKAGE,
        "deprecated shared domain has been split into shared.kernel and shared.platform"
    );
  }

  @Test
  void sharedKernelDoesNotDependOnCoreInfrastructureOrSpring() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/shared/kernel",
        List.of(
            "org.craft.mcs.cap.core.",
            "org.craft.mcs.cap.infrastructure.",
            "org.springframework."
        )
    );
  }

  @Test
  void coreDoesNotDependOnInfrastructure() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core",
        List.of("org.craft.mcs.cap.infrastructure.")
    );
  }

  @Test
  void ciderAnalysisSupportContextsDoNotDependOnClassificationContext() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/profileaxis",
        List.of("org.craft.mcs.cap.core.cideranalysis.classification.")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/sugaracidity",
        List.of("org.craft.mcs.cap.core.cideranalysis.classification.")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/profiletarget",
        List.of("org.craft.mcs.cap.core.cideranalysis.classification.")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/composition",
        List.of("org.craft.mcs.cap.core.cideranalysis.classification.")
    );
  }

  @Test
  void lowerLevelCiderAnalysisContextsDoNotDependOnCompositionContexts() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/profileaxis",
        List.of(
            "org.craft.mcs.cap.core.cideranalysis.sugaracidity.",
            "org.craft.mcs.cap.core.cideranalysis.profiletarget.",
            "org.craft.mcs.cap.core.cideranalysis.composition."
        )
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/sugaracidity",
        List.of(
            "org.craft.mcs.cap.core.cideranalysis.profiletarget.",
            "org.craft.mcs.cap.core.cideranalysis.composition."
        )
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/profiletarget",
        List.of("org.craft.mcs.cap.core.cideranalysis.composition.")
    );
  }

  @Test
  void compositionCalculationDoesNotDependOnCompositionOptimization() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/composition/calculation",
        List.of("org.craft.mcs.cap.core.cideranalysis.composition.optimization.")
    );
  }

  private static void assertNoTextInMainSources(
      @NonNull String forbiddenText,
      @NonNull String reason
  ) throws IOException {
    final var violations = sourceFilesUnder("")
        .stream()
        .filter(source -> source.content().contains(forbiddenText))
        .map(source -> "%s contains %s: %s".formatted(
            source.path(),
            forbiddenText,
            reason
        ))
        .toList();

    assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
  }

  private static void assertNoTextInMainResources(
      @NonNull String forbiddenText,
      @NonNull String reason
  ) throws IOException {
    if (Files.notExists(MAIN_RESOURCES)) {
      return;
    }

    try (var files = Files.walk(MAIN_RESOURCES)) {
      final var violations = files
          .filter(Files::isRegularFile)
          .filter(PackageBoundaryTest::isTextResource)
          .map(PackageBoundaryTest::readSourceFile)
          .filter(source -> source.content().contains(forbiddenText))
          .map(source -> "%s contains %s: %s".formatted(
              source.path(),
              forbiddenText,
              reason
          ))
          .toList();

      assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
    }
  }

  private static void assertNoImportsUnder(
      @NonNull String relativePackagePath,
      @NonNull List<@NonNull String> forbiddenImports
  ) throws IOException {
    final var violations = sourceFilesUnder(relativePackagePath)
        .stream()
        .flatMap(source -> forbiddenImports
            .stream()
            .filter(forbiddenImport -> source.content().contains("import " + forbiddenImport))
            .map(forbiddenImport -> "%s imports forbidden package %s".formatted(
                source.path(),
                forbiddenImport
            )))
        .toList();

    assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
  }

  private static @NonNull List<@NonNull SourceFile> sourceFilesUnder(
      @NonNull String relativePackagePath
  ) throws IOException {
    final var sourceRoot = MAIN_SOURCES.resolve(relativePackagePath);
    if (Files.notExists(sourceRoot)) {
      return List.of();
    }

    try (var files = Files.walk(sourceRoot)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .map(PackageBoundaryTest::readSourceFile)
          .toList();
    }
  }

  private static @NonNull SourceFile readSourceFile(@NonNull Path path) {
    try {
      return new SourceFile(path, Files.readString(path));
    } catch (IOException exception) {
      throw new IllegalStateException("failed to read source file: " + path, exception);
    }
  }

  private static boolean isTextResource(@NonNull Path path) {
    final var normalized = path.toString();

    return normalized.endsWith(".yaml")
        || normalized.endsWith(".yml")
        || normalized.endsWith(".xml")
        || normalized.endsWith(".sql")
        || normalized.endsWith(".json")
        || normalized.contains("/META-INF/services/");
  }

  private record SourceFile(
      @NonNull Path path,
      @NonNull String content
  ) {
  }
}
