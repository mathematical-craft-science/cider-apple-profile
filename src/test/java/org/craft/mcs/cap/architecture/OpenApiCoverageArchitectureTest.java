package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.swagger.v3.oas.annotations.Operation;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap.infrastructure.adapter.in.rest",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class OpenApiCoverageArchitectureTest {

  @ArchTest
  static final ArchRule rest_controllers_implement_matching_openapi_contract =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .and()
          .haveSimpleNameEndingWith("Controller")
          .should(implementMatchingApiContract());

  @ArchTest
  static final ArchRule openapi_contract_methods_are_documented =
      classes()
          .that()
          .areInterfaces()
          .and()
          .haveSimpleNameEndingWith("Api")
          .should(haveOperationOnEveryContractMethod());

  private static @NonNull ArchCondition<JavaClass> implementMatchingApiContract() {
    return new ArchCondition<>("implement a same-package OpenAPI Api interface") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var expectedApiName = item.getSimpleName().replaceFirst("Controller$", "Api");
        final var implementedApis = item.getInterfaces().stream()
            .map(api -> api.getName().substring(api.getName().lastIndexOf('.') + 1))
            .collect(Collectors.toSet());

        events.add(new SimpleConditionEvent(
            item,
            implementedApis.contains(expectedApiName),
            "%s must implement %s; implemented interfaces: %s"
                .formatted(item.getName(), expectedApiName, implementedApis)
        ));
      }
    };
  }

  private static @NonNull ArchCondition<JavaClass> haveOperationOnEveryContractMethod() {
    return new ArchCondition<>("declare @Operation on every endpoint contract method") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var undocumentedMethods = item.getMethods().stream()
            .filter(method -> method.getOwner().equals(item))
            .filter(method -> !method.isAnnotatedWith(Operation.class))
            .map(JavaMethod::getName)
            .sorted()
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            undocumentedMethods.isEmpty(),
            "%s must annotate every endpoint contract method with @Operation: %s"
                .formatted(item.getName(), String.join(", ", undocumentedMethods))
        ));
      }
    };
  }
}
