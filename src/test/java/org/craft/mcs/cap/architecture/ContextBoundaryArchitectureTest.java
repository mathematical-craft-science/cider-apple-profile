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
import java.util.Locale;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ContextBoundaryArchitectureTest {

  @ArchTest
  static final ArchRule shared_code_does_not_depend_on_core_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.shared..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core..",
              "org.craft.mcs.cap.infrastructure.."
          );

  @ArchTest
  static final ArchRule core_domain_does_not_depend_on_application_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core..application..",
              "org.craft.mcs.cap.infrastructure.."
          );

  @ArchTest
  static final ArchRule bounded_context_domains_do_not_depend_on_cider_analysis =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.appleprofile.domain..",
              "org.craft.mcs.cap.core.batchtraceability.domain..",
              "org.craft.mcs.cap.core.feedstock.domain..",
              "org.craft.mcs.cap.core.production.domain..",
              "org.craft.mcs.cap.core.varietyprofile.domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis..");

  @ArchTest
  static final ArchRule production_domain_does_not_depend_on_feedstock_domain =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.domain..");

  @ArchTest
  static final ArchRule production_domain_does_not_use_feedstock_batch_id_directly =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.domain..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatchId.class);

  @ArchTest
  static final ArchRule aggregate_repository_ports_do_not_expose_read_model_queries =
      classes()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..")
          .and()
          .haveSimpleNameEndingWith("Repository")
          .should(notDeclareReadModelQueryMethods());

  private static @NonNull ArchCondition<JavaClass> notDeclareReadModelQueryMethods() {
    return new ArchCondition<>("not declare read-model query methods") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        if (isReadModelRepository(item)) {
          events.add(SimpleConditionEvent.satisfied(
              item,
              item.getName() + " is an explicit read-model repository"
          ));
          return;
        }

        final var violations = item.getMethods()
            .stream()
            .filter(method -> method.getOwner().equals(item))
            .filter(ContextBoundaryArchitectureTest::hasReadModelQueryName)
            .map(JavaMethod::getName)
            .sorted()
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            violations.isEmpty(),
            "%s must keep read-model query methods out of aggregate repository ports: %s"
                .formatted(item.getName(), String.join(", ", violations))
        ));
      }
    };
  }

  private static boolean isReadModelRepository(@NonNull JavaClass item) {
    final var simpleName = item.getSimpleName();
    return simpleName.contains("Query")
        || simpleName.contains("Candidate");
  }

  private static boolean hasReadModelQueryName(@NonNull JavaMethod method) {
    final var name = method.getName().toLowerCase(Locale.ROOT);
    return name.contains("candidate")
        || name.contains("point")
        || name.contains("summary")
        || name.contains("projection")
        || name.contains("score")
        || name.contains("current")
        || name.contains("latest");
  }
}
