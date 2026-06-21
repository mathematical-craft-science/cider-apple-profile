package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.math.BigDecimal;
import java.util.Set;
import org.jspecify.annotations.NonNull;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


@AnalyzeClasses(
    packages = {
        "org.craft.mcs.cap.infrastructure.adapter.in.rest",
        "org.craft.mcs.cap.shared.infrastructure.rest"
    },
    importOptions = ImportOption.DoNotIncludeTests.class
)
class RestResponseQuantityArchitectureTest {

  private static final @NonNull Set<@NonNull String> ALLOWED_BARE_DECIMAL_RESPONSE_FIELDS = Set.of(
      "org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse.value",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationObjectiveAssessmentResponse.score",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltyResponse.value",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse.value",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceResponse.proportion",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationSourceResponse.proportion",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCompositionResponse.targetDistance",
      "org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchDetailsResponse.targetDistance"
  );

  @ArchTest
  static final ArchRule response_physical_quantities_are_not_bare_decimals =
      classes()
          .that()
          .haveSimpleNameEndingWith("Response")
          .should(notExposeBareBigDecimalFieldsOutsideAllowList());

  private static @NonNull ArchCondition<JavaClass> notExposeBareBigDecimalFieldsOutsideAllowList() {
    return new ArchCondition<>("not expose bare BigDecimal fields outside explicit dimensionless allow-list") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var violations = item.getFields()
            .stream()
            .filter(RestResponseQuantityArchitectureTest::isBareDecimalField)
            .map(field -> "%s.%s".formatted(field.getOwner().getName(), field.getName()))
            .filter(fieldName -> !ALLOWED_BARE_DECIMAL_RESPONSE_FIELDS.contains(fieldName))
            .sorted()
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            violations.isEmpty(),
            "%s must wrap physical decimal response fields into DecimalQuantityResponse: %s"
                .formatted(item.getName(), String.join(", ", violations))
        ));
      }
    };
  }

  private static boolean isBareDecimalField(@NonNull JavaField field) {
    return field.getRawType().isEquivalentTo(BigDecimal.class);
  }
}
