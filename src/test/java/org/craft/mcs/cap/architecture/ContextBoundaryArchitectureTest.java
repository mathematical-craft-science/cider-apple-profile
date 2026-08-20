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
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceCharacteristics;
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
  static final ArchRule core_code_does_not_depend_on_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.infrastructure..");

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
  static final ArchRule production_bounded_contexts_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.production.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule feedstock_bounded_contexts_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.feedstock.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule apple_profile_bounded_contexts_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.appleprofile.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule domain_area_roots_are_namespaces_only =
      noClasses()
          .that()
          .doNotHaveSimpleName("package-info")
          .should()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.production",
              "org.craft.mcs.cap.core.feedstock",
              "org.craft.mcs.cap.core.appleprofile",
              "org.craft.mcs.cap.core.cideranalysis",
              "org.craft.mcs.cap.core.formulation"
          );

  @ArchTest
  static final ArchRule cider_analysis_does_not_depend_on_material_formulation_or_catalog_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..",
              "org.craft.mcs.cap.core.appleprofile..",
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.formulation..",
              "org.craft.mcs.cap.core.measurement..",
              "org.craft.mcs.cap.core.production..",
              "org.craft.mcs.cap.core.traceability..",
              "org.craft.mcs.cap.core.varietyprofile..",
              "org.craft.mcs.cap.core.vessel.."
          );

  @ArchTest
  static final ArchRule formulation_does_not_depend_on_material_or_catalog_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.formulation..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..",
              "org.craft.mcs.cap.core.appleprofile..",
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.measurement..",
              "org.craft.mcs.cap.core.production..",
              "org.craft.mcs.cap.core.traceability..",
              "org.craft.mcs.cap.core.varietyprofile..",
              "org.craft.mcs.cap.core.vessel.."
          );

  @ArchTest
  static final ArchRule formulation_uses_cider_analysis_only_through_infrastructure_acl =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.formulation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis..");

  @ArchTest
  static final ArchRule cider_analysis_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.cideranalysis.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule formulation_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.formulation.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule formulation_calculation_does_not_depend_on_higher_level_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.formulation.calculation..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.formulation.optimization..",
              "org.craft.mcs.cap.core.formulation.interpretation.."
          );

  @ArchTest
  static final ArchRule formulation_optimization_does_not_depend_on_interpretation =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.formulation.optimization..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.formulation.interpretation..");

  @ArchTest
  static final ArchRule consumer_contexts_use_calculation_contexts_only_through_infrastructure_acl =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..",
              "org.craft.mcs.cap.core.appleprofile..",
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.measurement..",
              "org.craft.mcs.cap.core.production..",
              "org.craft.mcs.cap.core.traceability..",
              "org.craft.mcs.cap.core.varietyprofile..",
              "org.craft.mcs.cap.core.vessel.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..",
              "org.craft.mcs.cap.core.formulation.."
          );

  @ArchTest
  static final ArchRule calculation_contexts_do_not_depend_on_persistence_or_transactions =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..",
              "org.craft.mcs.cap.core.formulation.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence..",
              "org.craft.mcs.cap.shared.platform..",
              "org.springframework.jdbc..",
              "org.springframework.data..",
              "org.springframework.transaction.."
          );

  @ArchTest
  static final ArchRule calculation_contexts_do_not_declare_repositories =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..",
              "org.craft.mcs.cap.core.formulation.."
          )
          .should()
          .haveSimpleNameEndingWith("Repository");

  @ArchTest
  static final ArchRule graph_domain_does_not_depend_on_cider_apple_classification =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.graph..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.classification..");

  @ArchTest
  static final ArchRule cider_apple_classification_does_not_depend_on_graph =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.classification..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.graph..");

  @ArchTest
  static final ArchRule cider_analysis_abv_does_not_depend_on_peer_modules =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.abv..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis.classification..",
              "org.craft.mcs.cap.core.cideranalysis.graph..",
              "org.craft.mcs.cap.core.cideranalysis.readiness..",
              "org.craft.mcs.cap.core.cideranalysis.targetfit.."
          );

  @ArchTest
  static final ArchRule cider_analysis_classification_does_not_depend_on_peer_modules =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.classification..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis.abv..",
              "org.craft.mcs.cap.core.cideranalysis.graph..",
              "org.craft.mcs.cap.core.cideranalysis.readiness..",
              "org.craft.mcs.cap.core.cideranalysis.targetfit.."
          );

  @ArchTest
  static final ArchRule cider_analysis_graph_does_not_depend_on_peer_modules =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.graph..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis.abv..",
              "org.craft.mcs.cap.core.cideranalysis.classification..",
              "org.craft.mcs.cap.core.cideranalysis.readiness..",
              "org.craft.mcs.cap.core.cideranalysis.targetfit.."
          );

  @ArchTest
  static final ArchRule cider_analysis_readiness_does_not_depend_on_peer_modules =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.readiness..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis.abv..",
              "org.craft.mcs.cap.core.cideranalysis.classification..",
              "org.craft.mcs.cap.core.cideranalysis.graph..",
              "org.craft.mcs.cap.core.cideranalysis.targetfit.."
          );

  @ArchTest
  static final ArchRule target_fit_depends_only_on_graph_within_cider_analysis =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.cideranalysis.targetfit..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis.abv..",
              "org.craft.mcs.cap.core.cideranalysis.classification..",
              "org.craft.mcs.cap.core.cideranalysis.readiness.."
          );

  @ArchTest
  static final ArchRule feedstock_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.feedstock.batch.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule feedstock_analysis_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.feedstock.batch.application.analysis.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule apple_profile_analysis_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.appleprofile.analysis.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule apple_profile_catalog_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.appleprofile.catalog.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule feedstock_planning_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.feedstock.planning.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule production_batch_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.production.batch.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule production_planning_application_slices_are_free_of_cycles =
      slices()
          .matching("org.craft.mcs.cap.core.production.planning.application.(*)..")
          .should()
          .beFreeOfCycles();

  @ArchTest
  static final ArchRule feedstock_measurement_application_does_not_depend_on_analysis =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.application.measurement..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.application.analysis..");

  @ArchTest
  static final ArchRule feedstock_batch_does_not_depend_on_planning =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.planning..");

  @ArchTest
  static final ArchRule feedstock_planning_uses_only_published_batch_application_contracts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.planning..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.application..")
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.feedstock.batch.application.api.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.."
                  ))
          );

  @ArchTest
  static final ArchRule feedstock_batch_published_contracts_do_not_expose_aggregate =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock.batch.application.api..",
              "org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.."
          )
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatch.class);

  @ArchTest
  static final ArchRule production_planning_does_not_depend_on_batch_application =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.planning..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.production.batch.application..");

  @ArchTest
  static final ArchRule production_batch_application_contracts_do_not_expose_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.batch.application.contract..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(ProductionBatch.class);

  @ArchTest
  static final ArchRule feedstock_planning_does_not_depend_on_feedstock_batch_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.planning..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatch.class);

  @ArchTest
  static final ArchRule feedstock_planning_uses_only_published_batch_domain_language =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock.planning..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.domain..")
                  .and(
                      JavaClass.Predicates.equivalentTo(FeedstockBatchId.class)
                          .or(JavaClass.Predicates.equivalentTo(FeedstockSeason.class))
                          .negate()
                  )
          );

  @ArchTest
  static final ArchRule apple_profile_catalog_does_not_depend_on_analysis_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis..");

  @ArchTest
  static final ArchRule apple_profile_analysis_does_not_depend_on_catalog_application =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog.application..");

  @ArchTest
  static final ArchRule apple_profile_analysis_does_not_depend_on_apple_variety_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(AppleVariety.class);

  @ArchTest
  static final ArchRule apple_profile_analysis_uses_only_published_catalog_identity =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog.domain..")
                  .and(JavaClass.Predicates.equivalentTo(AppleVarietyId.class).negate())
          );

  @ArchTest
  static final ArchRule apple_profile_catalog_reference_contract_does_not_expose_domain_model =
      noClasses()
          .that()
          .areAssignableTo(AppleVarietyReferenceProfileView.class)
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog.domain..")
                  .and(JavaClass.Predicates.equivalentTo(AppleVarietyId.class).negate())
          );

  @ArchTest
  static final ArchRule feedstock_planning_acl_uses_batch_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock.batch.application..usecase..",
              "org.craft.mcs.cap.core.feedstock.batch.application..query..",
              "org.craft.mcs.cap.core.feedstock.batch.application..service..",
              "org.craft.mcs.cap.core.feedstock.batch.application..port.out..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.."
          );

  @ArchTest
  static final ArchRule feedstock_planning_acl_uses_only_published_batch_application_boundary =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.application..")
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.feedstock.batch.application.api.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.feedstock.batch.application..port.in.."
                  ))
          );

  @ArchTest
  static final ArchRule feedstock_planning_acl_does_not_access_batch_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatch.class);

  @ArchTest
  static final ArchRule feedstock_planning_acl_uses_only_published_batch_domain_language =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.feedstock.batch.domain..")
                  .and(JavaClass.Predicates.equivalentTo(FeedstockSeason.class).negate())
          );

  @ArchTest
  static final ArchRule production_planning_acl_uses_batch_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.production.planning..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.production.batch.application..usecase..",
              "org.craft.mcs.cap.core.production.batch.application..port.out..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch.."
          );

  @ArchTest
  static final ArchRule production_planning_acl_uses_only_published_batch_application_boundary =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.production.planning..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.production.batch.application..")
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.production.batch.application..port.in.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.production.batch.application.contract.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.production.batch.application"
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.production.batch.application.creation"
                  ))
          );

  @ArchTest
  static final ArchRule production_planning_acl_does_not_access_batch_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.production.planning..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(ProductionBatch.class);

  @ArchTest
  static final ArchRule production_planning_source_acl_does_not_use_batch_application_language =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.production.batch.application..");

  @ArchTest
  static final ArchRule apple_profile_analysis_acl_uses_catalog_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.appleprofile.catalog.application..usecase..",
              "org.craft.mcs.cap.core.appleprofile.catalog.application..port.out..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.."
          );

  @ArchTest
  static final ArchRule apple_profile_analysis_acl_uses_only_published_catalog_application_boundary =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog.application..")
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.appleprofile.catalog.application.contract.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.appleprofile.catalog.application..port.in.."
                  ))
                  .and(JavaClass.Predicates.resideOutsideOfPackage(
                      "org.craft.mcs.cap.core.appleprofile.catalog.application"
                  ))
          );

  @ArchTest
  static final ArchRule apple_profile_analysis_acl_does_not_access_catalog_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(AppleVariety.class);

  @ArchTest
  static final ArchRule apple_profile_analysis_acl_uses_only_published_catalog_identity =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates
                  .resideInAPackage("org.craft.mcs.cap.core.appleprofile.catalog.domain..")
                  .and(JavaClass.Predicates.equivalentTo(AppleVarietyId.class).negate())
          );

  @ArchTest
  static final ArchRule apple_profile_target_analysis_does_not_depend_on_graph_endpoint_slice =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity..");

  @ArchTest
  static final ArchRule apple_profile_graph_endpoint_slice_does_not_depend_on_target_analysis =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget..");

  @ArchTest
  static final ArchRule apple_profile_graph_bricks_do_not_depend_on_scenario_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.graph..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget..",
              "org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.."
          );

  @ArchTest
  static final ArchRule apple_profile_analysis_profile_slice_does_not_depend_on_scenario_slices =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.appleprofile.analysis.application.profile..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.appleprofile.analysis.application.abv..",
              "org.craft.mcs.cap.core.appleprofile.analysis.application.classification..",
              "org.craft.mcs.cap.core.appleprofile.analysis.application.graph..",
              "org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget..",
              "org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.."
          );

  @ArchTest
  static final ArchRule cider_analysis_and_formulation_domains_do_not_depend_on_application =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..domain..",
              "org.craft.mcs.cap.core.formulation..domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application..",
              "org.craft.mcs.cap.core.formulation..application.."
          );

  @ArchTest
  static final ArchRule calculation_context_application_contracts_do_not_depend_on_inbound_commands =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application.contract..",
              "org.craft.mcs.cap.core.formulation..application.contract.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application.port.in..",
              "org.craft.mcs.cap.core.formulation..application.port.in.."
          );

  @ArchTest
  static final ArchRule calculation_context_published_boundaries_do_not_expose_domain_model =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application.contract..",
              "org.craft.mcs.cap.core.cideranalysis..application.port.in..",
              "org.craft.mcs.cap.core.formulation..application.contract..",
              "org.craft.mcs.cap.core.formulation..application.port.in.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..domain..",
              "org.craft.mcs.cap.core.formulation..domain.."
          );

  @ArchTest
  static final ArchRule calculation_context_application_orchestrators_use_usecase_packages =
      noClasses()
          .should()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application.service..",
              "org.craft.mcs.cap.core.formulation..application.service.."
          );

  @ArchTest
  static final ArchRule application_layer_does_not_use_model_packages =
      noClasses()
          .should()
          .resideInAPackage("org.craft.mcs.cap.core..application..model..");

  @ArchTest
  static final ArchRule universal_chemical_profile_is_absent_from_main_code =
      noClasses()
          .should()
          .haveFullyQualifiedName("org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile");

  @ArchTest
  static final ArchRule universal_chemical_axis_abstractions_are_absent_from_main_code =
      noClasses()
          .should()
          .resideInAPackage("org.craft.mcs.cap.shared.kernel.chemical.axis..");

  @ArchTest
  static final ArchRule material_and_catalog_domains_do_not_depend_on_calculation_contexts =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..domain..",
              "org.craft.mcs.cap.core.appleprofile..domain..",
              "org.craft.mcs.cap.core.traceability.domain..",
              "org.craft.mcs.cap.core.feedstock..domain..",
              "org.craft.mcs.cap.core.measurement.domain..",
              "org.craft.mcs.cap.core.production..domain..",
              "org.craft.mcs.cap.core.varietyprofile.domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..",
              "org.craft.mcs.cap.core.formulation.."
          );

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
  static final ArchRule inbound_adapters_do_not_depend_on_outbound_application_ports =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.out..");

  @ArchTest
  static final ArchRule inbound_adapters_do_not_depend_on_outbound_adapters =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out..");

  @ArchTest
  static final ArchRule outbound_adapters_do_not_depend_on_inbound_adapters =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in..");

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
          .resideInAPackage("org.craft.mcs.cap.core.feedstock..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.account..");

  @ArchTest
  static final ArchRule production_application_does_not_depend_on_account_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.account..");

  @ArchTest
  static final ArchRule measurement_context_does_not_depend_on_subject_owner_or_traceability_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.measurement..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..",
              "org.craft.mcs.cap.core.traceability..",
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.production.."
          );

  @ArchTest
  static final ArchRule measurement_subject_acl_uses_owner_inbound_boundaries_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.measurement..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.account..application..port.out..",
              "org.craft.mcs.cap.core.account..application..usecase..",
              "org.craft.mcs.cap.core.traceability.application..port.out..",
              "org.craft.mcs.cap.core.traceability.application..usecase..",
              "org.craft.mcs.cap.core.feedstock.batch.application..port.out..",
              "org.craft.mcs.cap.core.feedstock.batch.application..usecase..",
              "org.craft.mcs.cap.core.production.batch.application..port.out..",
              "org.craft.mcs.cap.core.production.batch.application..usecase..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.."
          );

  @ArchTest
  static final ArchRule measurement_lineage_acl_uses_traceability_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.traceability.measurement..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.traceability.application..port.out..",
              "org.craft.mcs.cap.core.traceability.application..usecase..",
              "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.."
          );

  @ArchTest
  static final ArchRule jdbc_persistence_adapters_do_not_call_inbound_use_cases =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core..application..port.in..");

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
              "org.craft.mcs.cap.core.account.administration..",
              "org.craft.mcs.cap.core.account.actor.."
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
              "org.craft.mcs.cap.core.account.administration..",
              "org.craft.mcs.cap.core.account.actor.."
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
              "org.craft.mcs.cap.core.account.administration..",
              "org.craft.mcs.cap.core.account.actor.."
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
  static final ArchRule production_source_boundary_does_not_expose_composition_input_model =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(CompositionSourceCharacteristics.class);

  @ArchTest
  static final ArchRule foundational_production_layers_do_not_depend_on_candidate_workflow =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.production.batch.domain..",
              "org.craft.mcs.cap.core.production.batch.application.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.production.planning..");

  @ArchTest
  static final ArchRule production_planning_does_not_depend_on_production_batch_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production.planning..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(ProductionBatch.class);

  @ArchTest
  static final ArchRule feedstock_context_does_not_depend_on_production_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.production..");

  @ArchTest
  static final ArchRule feedstock_context_does_not_depend_on_measurement_or_traceability_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.feedstock..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.measurement..",
              "org.craft.mcs.cap.core.traceability.."
          );

  @ArchTest
  static final ArchRule production_context_does_not_depend_on_measurement_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.measurement..");

  @ArchTest
  static final ArchRule production_context_does_not_depend_on_traceability_context =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.craft.mcs.cap.core.traceability..");

  @ArchTest
  static final ArchRule material_aggregate_domains_do_not_depend_on_calculation_contexts =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock..domain..",
              "org.craft.mcs.cap.core.production..domain.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..",
              "org.craft.mcs.cap.core.formulation.."
          );

  @ArchTest
  static final ArchRule traceability_context_does_not_depend_on_material_owner_or_measurement_contexts =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.traceability..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock..",
              "org.craft.mcs.cap.core.production..",
              "org.craft.mcs.cap.core.measurement.."
          );

  @ArchTest
  static final ArchRule production_source_acl_uses_feedstock_inbound_boundary_only =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source..",
              "org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.feedstock.batch.application..port.out..",
              "org.craft.mcs.cap.core.feedstock.batch.application..query.."
          );

  @ArchTest
  static final ArchRule production_source_acl_does_not_depend_on_feedstock_aggregate =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source..",
              "org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source.."
          )
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatch.class);

  @ArchTest
  static final ArchRule production_context_does_not_use_feedstock_batch_id_directly =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(FeedstockBatchId.class);

  @ArchTest
  static final ArchRule production_inbound_ports_do_not_expose_the_production_aggregate =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.core.production..application..port.in..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(ProductionBatch.class);

  @ArchTest
  static final ArchRule rest_controllers_do_not_execute_calculation_domain_services =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in.rest..")
          .and()
          .haveSimpleNameEndingWith("Controller")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..domain..",
              "org.craft.mcs.cap.core.formulation..domain.."
          );

  @ArchTest
  static final ArchRule rest_adapters_do_not_depend_on_calculation_domain =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter.in.rest..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..domain..",
              "org.craft.mcs.cap.core.formulation..domain.."
          );

  @ArchTest
  static final ArchRule calculation_context_acl_adapters_use_only_published_application_contracts =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis..",
              "org.craft.mcs.cap.infrastructure.adapter.out.formulation..",
              "org.craft.mcs.cap.infrastructure.adapter.out.production.planning.composition.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..domain..",
              "org.craft.mcs.cap.core.formulation..domain.."
          );

  @ArchTest
  static final ArchRule calculation_context_acl_adapters_do_not_manage_transactions =
      noClasses()
          .that()
          .resideInAnyPackage(
              "org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis..",
              "org.craft.mcs.cap.infrastructure.adapter.out.formulation.."
          )
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework.transaction..");

  @ArchTest
  static final ArchRule calculation_context_adapters_do_not_depend_on_application_implementations =
      noClasses()
          .that()
          .resideInAPackage("org.craft.mcs.cap.infrastructure.adapter..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.craft.mcs.cap.core.cideranalysis..application.usecase..",
              "org.craft.mcs.cap.core.cideranalysis..application.service..",
              "org.craft.mcs.cap.core.formulation..application.usecase..",
              "org.craft.mcs.cap.core.formulation..application.service..",
              "org.craft.mcs.cap.core.formulation..application.support.."
          );

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
        MeasurementSet.class.getName(),
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
