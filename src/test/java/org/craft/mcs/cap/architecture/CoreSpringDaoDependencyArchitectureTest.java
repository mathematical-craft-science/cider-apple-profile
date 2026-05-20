package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap.core",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class CoreSpringDaoDependencyArchitectureTest {

  @ArchTest
  static final ArchRule core_must_not_depend_on_spring_dao_exceptions =
      noClasses()
          .that()
          .resideInAnyPackage("org.craft.mcs.cap.core..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework.dao..");
}
