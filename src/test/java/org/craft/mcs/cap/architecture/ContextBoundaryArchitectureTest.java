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
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

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
  static final ArchRule core_does_not_depend_on_shared_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.shared.infrastructure..");

  @ArchTest
  static final ArchRule core_domain_is_framework_independent =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "jakarta.validation.."
          );

  @ArchTest
  static final ArchRule application_ports_do_not_depend_on_the_opposite_port_direction =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.in..");

  @ArchTest
  static final ArchRule inbound_application_ports_do_not_depend_on_outbound_ports =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..");

  @ArchTest
  static final ArchRule top_level_core_contexts_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule bounded_context_domains_do_not_depend_on_cider_analysis =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..domain..",
              "org.craft.mcs.cap.core.appleprofile.domain..",
              "org.craft.mcs.cap.core.batchtraceability.domain..",
              "org.craft.mcs.cap.core.feedstock.domain..",
              "org.craft.mcs.cap.core.production.domain..",
              "org.craft.mcs.cap.core.varietyprofile.domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.profileanalysis..");

  @ArchTest
  static final ArchRule core_and_shared_do_not_depend_on_spring_security =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core..",
              "org.craft.mcs.cap.shared.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework.security..");

  @ArchTest
  static final ArchRule main_source_code_does_not_generate_uuid_version_4 =
      noClasses()
          .should()
          .callMethod(UUID.class, "randomUUID");

  @ArchTest
  static final ArchRule core_and_shared_do_not_read_the_system_clock_directly =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core..",
              "org.craft.mcs.cap.shared.."
          )
          .should()
          .callMethod(Instant.class, "now");

  @ArchTest
  static final ArchRule feedstock_application_does_not_depend_on_account_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.account..");

  @ArchTest
  static final ArchRule production_application_does_not_depend_on_account_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.account..");

  @ArchTest
  static final ArchRule account_context_does_not_depend_on_feedstock_or_production_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.account..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.production.."
          );

  @ArchTest
  static final ArchRule account_shared_slice_does_not_depend_on_account_feature_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.account.shared..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account.user..",
              "org.craft.mcs.cap.core.account.workspace..",
              "org.craft.mcs.cap.core.account.membership..",
              "org.craft.mcs.cap.core.account.invitation..",
              "org.craft.mcs.cap.core.account.audit..",
              "org.craft.mcs.cap.core.account.contributor..",
              "org.craft.mcs.cap.core.account.provisioning..",
              "org.craft.mcs.cap.core.account.context..",
              "org.craft.mcs.cap.core.account.administration.."
          );

  @ArchTest
  static final ArchRule account_feature_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.account.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule foundational_account_domains_do_not_depend_on_process_slices =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account.user..domain..",
              "org.craft.mcs.cap.core.account.workspace..domain..",
              "org.craft.mcs.cap.core.account.membership..domain..",
              "org.craft.mcs.cap.core.account.contributor..domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account.invitation..",
              "org.craft.mcs.cap.core.account.audit..",
              "org.craft.mcs.cap.core.account.provisioning..",
              "org.craft.mcs.cap.core.account.context..",
              "org.craft.mcs.cap.core.account.administration.."
          );

  @ArchTest
  static final ArchRule account_audit_slice_does_not_depend_on_audited_feature_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.account.audit..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account.user..",
              "org.craft.mcs.cap.core.account.workspace..",
              "org.craft.mcs.cap.core.account.membership..",
              "org.craft.mcs.cap.core.account.invitation..",
              "org.craft.mcs.cap.core.account.contributor..",
              "org.craft.mcs.cap.core.account.provisioning..",
              "org.craft.mcs.cap.core.account.context..",
              "org.craft.mcs.cap.core.account.administration.."
          );

  @ArchTest
  static final ArchRule account_feature_slices_depend_on_audit_port_instead_of_implementation =
      noClasses()
          .that()
          .resideOutsideOfPackage("org.craft.mcs.cap.core.account.audit..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.account.audit.application.usecase..");

  @ArchTest
  static final ArchRule production_context_does_not_depend_on_feedstock_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock..");

  @ArchTest
  static final ArchRule production_source_acl_uses_feedstock_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.production.source..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock.application..port.out..",
              "org.craft.mcs.cap.core.feedstock.application..query..",
              "org.craft.mcs.cap.core.feedstock.domain.model.batch..",
              "org.craft.mcs.cap.core.feedstock.domain.model.measurement.."
          );

  @ArchTest
  static final ArchRule production_domain_does_not_use_feedstock_batch_id_directly =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.domain..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatchId.class);

  @ArchTest
  static final ArchRule production_inbound_ports_do_not_expose_the_production_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.application.port.in..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(ProductionBatch.class);

  @ArchTest
  static final ArchRule rest_controllers_do_not_execute_profile_analysis_domain_services =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in.rest..")
          .and()
          .haveSimpleNameEndingWith("Controller")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.profileanalysis..domain..");

  @ArchTest
  static final ArchRule aggregate_repository_ports_do_not_expose_read_model_queries =
      classes()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..")
          .and()
          .haveSimpleNameEndingWith("Repository")
          .should(notDeclareReadModelQueryMethods());

  @ArchTest
  static final ArchRule read_model_repository_ports_do_not_depend_on_aggregate_roots =
      classes()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..")
          .should(notDependOnAggregateRootsWhenReadModelRepository());

  private static @NonNull ArchCondition<JavaClass> notDependOnAggregateRootsWhenReadModelRepository() {
    final var aggregateRootNames = Set.of(
        AppleVariety.class.getName(),
        FeedstockBatch.class.getName(),
        FeedstockMeasurement.class.getName(),
        ProductionBatch.class.getName()
    );

    return new ArchCondition<>("not depend on aggregate roots when it is a read-model repository") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        if (!isReadModelRepository(item)) {
          events.add(SimpleConditionEvent.satisfied(item, item.getName() + " is an aggregate repository"));
          return;
        }

        final var aggregateDependencies = item.getDirectDependenciesFromSelf()
            .stream()
            .map(dependency -> dependency.getTargetClass().getName())
            .filter(aggregateRootNames::contains)
            .distinct()
            .sorted()
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            aggregateDependencies.isEmpty(),
            "%s must expose immutable views instead of aggregate roots: %s"
                .formatted(item.getName(), String.join(", ", aggregateDependencies))
        ));
      }
    };
  }

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
    return name.equals("findall")
        || name.contains("candidate")
        || name.contains("point")
        || name.contains("summary")
        || name.contains("projection")
        || name.contains("score")
        || name.contains("current")
        || name.contains("latest");
  }
}
