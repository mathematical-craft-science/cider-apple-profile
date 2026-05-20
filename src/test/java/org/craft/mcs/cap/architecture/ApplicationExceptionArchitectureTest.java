package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.craft.mcs.cap.shared.application.ApplicationException;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap.core",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ApplicationExceptionArchitectureTest {

  @ArchTest
  static final ArchRule application_exceptions_extend_application_exception =
      classes()
          .that()
          .resideInAnyPackage("org.craft.mcs.cap.core..application..")
          .and()
          .haveSimpleNameEndingWith("Exception")
          .should()
          .beAssignableTo(ApplicationException.class);
}
