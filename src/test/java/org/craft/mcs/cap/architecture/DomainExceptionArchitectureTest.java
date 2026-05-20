package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class DomainExceptionArchitectureTest {

  @ArchTest
  static final ArchRule domain_code_uses_domain_exceptions_instead_of_technical_argument_exceptions =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core..domain..",
              "org.craft.mcs.cap.shared.kernel.."
          )
          .should()
          .dependOnClassesThat()
          .areAssignableTo(IllegalArgumentException.class);
}
