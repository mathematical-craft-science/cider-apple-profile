package org.craft.mcs.cap.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageBoundaryTest {

  private static final @NonNull Path MAIN_SOURCES = Path.of("src/main/java");
  private static final @NonNull Path MAIN_RESOURCES = Path.of("src/main/resources");
  private static final @NonNull String DEPRECATED_SHARED_DOMAIN_PACKAGE =
      "org.craft.mcs.cap.shared." + "domain";
  private static final @NonNull String DEPRECATED_ACCOUNT_APPLICATION_PACKAGE =
      "org.craft.mcs.cap.core.account." + "application";
  private static final @NonNull String DEPRECATED_ACCOUNT_DOMAIN_PACKAGE =
      "org.craft.mcs.cap.core.account." + "domain";

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
  void accountContextUsesFeatureFirstPackages() throws IOException {
    assertNoTextInMainSources(
        DEPRECATED_ACCOUNT_APPLICATION_PACKAGE,
        "account application code must belong to a feature slice"
    );
    assertNoTextInMainSources(
        DEPRECATED_ACCOUNT_DOMAIN_PACKAGE,
        "account domain code must belong to a feature slice"
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
  void sharedMaterialAndMeasurementTypesRemainContextNeutral() throws IOException {
    final var violations = new ArrayList<String>();
    for (final var path : List.of(
        "org/craft/mcs/cap/shared/kernel/material",
        "org/craft/mcs/cap/shared/application/measurement"
    )) {
      sourceFilesUnder(path).stream()
          .map(source -> source.path().getFileName().toString())
          .filter(name -> name.contains("Feedstock") || name.contains("Production"))
          .forEach(violations::add);
    }
    violations.sort(String::compareTo);

    assertThat(violations)
        .as("shared material and measurement type names must remain context-neutral")
        .isEmpty();
  }

  @Test
  void coreDoesNotDependOnInfrastructure() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core",
        List.of("org.craft.mcs.cap.infrastructure.")
    );
  }

  @Test
  void infrastructureDoesNotOwnTransactionBoundaries() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/infrastructure",
        List.of(
            "org.springframework.transaction.PlatformTransactionManager",
            "org.springframework.transaction.annotation.Transactional",
            "org.springframework.transaction.support.TransactionTemplate"
        )
    );
  }

  @Test
  void materialBatchContextsDoNotDependOnVesselImplementation() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/feedstock",
        List.of("org.craft.mcs.cap.core.vessel.")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/production",
        List.of("org.craft.mcs.cap.core.vessel.")
    );
  }

  @Test
  void vesselContextDoesNotDependOnMaterialBatchContexts() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/vessel",
        List.of(
            "org.craft.mcs.cap.core.feedstock.",
            "org.craft.mcs.cap.core.production.",
            "org.craft.mcs.cap.core.measurement.",
            "org.craft.mcs.cap.core.traceability."
        )
    );
  }

  @Test
  void coreClassesUseExplicitApplicationOrDomainLayers() throws IOException {
    final var violations = sourceFilesUnder("org/craft/mcs/cap/core")
        .stream()
        .filter(source -> !usesExplicitCoreLayer(source.path()))
        .map(source -> source.path().toString())
        .toList();

    assertThat(violations)
        .as("core classes outside explicit application or domain layers")
        .isEmpty();
  }

  @Test
  void ciderAnalysisDoesNotDependOnFormulation() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis",
        List.of("org.craft.mcs.cap.core.formulation.")
    );
  }

  @Test
  void ciderAnalysisModulesUseOnlyThePublishedDependencyDirection() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/abv",
        ciderAnalysisModulesExcept("abv")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/classification",
        ciderAnalysisModulesExcept("classification")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/graph",
        ciderAnalysisModulesExcept("graph")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/readiness",
        ciderAnalysisModulesExcept("readiness")
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis/targetfit",
        List.of(
            "org.craft.mcs.cap.core.cideranalysis.abv.",
            "org.craft.mcs.cap.core.cideranalysis.classification.",
            "org.craft.mcs.cap.core.cideranalysis.readiness."
        )
    );
  }

  @Test
  void calculationContextsDoNotDependOnPersistenceOrTransactions() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/cideranalysis",
        List.of(
            "org.craft.mcs.cap.infrastructure.adapter.out.persistence.",
            "org.springframework.data.",
            "org.springframework.jdbc.",
            "org.springframework.transaction."
        )
    );
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/formulation",
        List.of(
            "org.craft.mcs.cap.infrastructure.adapter.out.persistence.",
            "org.springframework.data.",
            "org.springframework.jdbc.",
            "org.springframework.transaction."
        )
    );
  }

  @Test
  void consumerCoreUsesCalculationContextsOnlyThroughInfrastructureAcl() throws IOException {
    final var forbiddenImports = List.of(
        "org.craft.mcs.cap.core.cideranalysis.",
        "org.craft.mcs.cap.core.formulation."
    );
    assertNoImportsUnder("org/craft/mcs/cap/core/appleprofile", forbiddenImports);
    assertNoImportsUnder("org/craft/mcs/cap/core/feedstock", forbiddenImports);
    assertNoImportsUnder("org/craft/mcs/cap/core/production", forbiddenImports);
  }

  @Test
  void compositionCalculationDoesNotDependOnHigherLevelFormulationSlices() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/formulation/calculation",
        List.of(
            "org.craft.mcs.cap.core.formulation.optimization.",
            "org.craft.mcs.cap.core.formulation.interpretation."
        )
    );
  }

  @Test
  void compositionOptimizationDoesNotDependOnInterpretation() throws IOException {
    assertNoImportsUnder(
        "org/craft/mcs/cap/core/formulation/optimization",
        List.of("org.craft.mcs.cap.core.formulation.interpretation.")
    );
  }

  private static @NonNull List<@NonNull String> ciderAnalysisModulesExcept(
      @NonNull String allowedModule
  ) {
    return List.of("abv", "classification", "graph", "readiness", "targetfit")
        .stream()
        .filter(module -> !module.equals(allowedModule))
        .map(module -> "org.craft.mcs.cap.core.cideranalysis." + module + ".")
        .toList();
  }

  @Test
  void jdbcContextsDoNotReadTablesOwnedByPeerContexts() throws IOException {
    assertNoTextUnder(
        "org/craft/mcs/cap/infrastructure/adapter/out/persistence/jdbc/measurement",
        List.of("feedstock_batches", "production_batches", "batch_trace_")
    );
    assertNoTextUnder(
        "org/craft/mcs/cap/infrastructure/adapter/out/persistence/jdbc/feedstock",
        List.of(
            "measurement_",
            "production_batches",
            "batch_trace_",
            "JOIN vessels",
            "FROM vessels"
        )
    );
    assertNoTextUnder(
        "org/craft/mcs/cap/infrastructure/adapter/out/persistence/jdbc/production",
        List.of("measurement_", "feedstock_batches", "batch_trace_", "JOIN vessels", "FROM vessels")
    );
    assertNoTextUnder(
        "org/craft/mcs/cap/infrastructure/adapter/out/persistence/jdbc/traceability",
        List.of("measurement_", "feedstock_batches", "production_batches")
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

    assertThat(violations)
        .withFailMessage(() -> String.join(System.lineSeparator(), violations))
        .isEmpty();
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

      assertThat(violations)
          .withFailMessage(() -> String.join(System.lineSeparator(), violations))
          .isEmpty();
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

    assertThat(violations)
        .withFailMessage(() -> String.join(System.lineSeparator(), violations))
        .isEmpty();
  }

  private static void assertNoTextUnder(
      @NonNull String relativePackagePath,
      @NonNull List<@NonNull String> forbiddenText
  ) throws IOException {
    final var violations = sourceFilesUnder(relativePackagePath)
        .stream()
        .flatMap(source -> forbiddenText.stream()
            .filter(source.content()::contains)
            .map(text -> "%s contains foreign schema reference %s".formatted(source.path(), text)))
        .toList();

    assertThat(violations)
        .withFailMessage(() -> String.join(System.lineSeparator(), violations))
        .isEmpty();
  }

  private static boolean usesExplicitCoreLayer(@NonNull Path path) {
    final var normalized = path.toString().replace('\\', '/');
    return normalized.contains("/application/")
        || normalized.contains("/domain/")
        || normalized.endsWith("/package-info.java");
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
