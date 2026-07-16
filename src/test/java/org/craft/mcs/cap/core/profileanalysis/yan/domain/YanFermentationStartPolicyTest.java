package org.craft.mcs.cap.core.profileanalysis.yan.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheck;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheckCode;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class YanFermentationStartPolicyTest {

  private final YanFermentationStartPolicy policy = YanFermentationStartPolicy.curatedCiderV1();

  @Test
  void returnsWarningWhenYanIsMissing() {
    final var check = policy.check(profileWithoutYan());

    assertThat(check)
        .returns(FermentationStartReadinessCheckCode.YAN_MISSING, FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.WARNING, FermentationStartReadinessCheck::severity)
        .returns(null, FermentationStartReadinessCheck::value);
  }

  @Test
  void returnsBlockerWhenYanIsSeverelyLowWithoutNutrientPlan() {
    final var check = policy.check(profile("60"));

    assertThat(check)
        .returns(FermentationStartReadinessCheckCode.YAN_SEVERELY_LOW, FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.BLOCKER, FermentationStartReadinessCheck::severity)
        .returns("mg N/L", FermentationStartReadinessCheck::unit)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("60"));
  }

  @Test
  void returnsManagedWarningWhenYanIsSeverelyLowWithNutrientPlan() {
    final var check = policy.check(new YanFermentationStartContext(
        profile("60"),
        Set.of(FermentationStartPreparationActionType.YEAST_NUTRIENT_ADDITION_PLANNED)
    ));

    assertThat(check)
        .returns(
            FermentationStartReadinessCheckCode.YAN_SEVERELY_LOW_MANAGED,
            FermentationStartReadinessCheck::code
        )
        .returns(FermentationStartReadinessSeverity.WARNING, FermentationStartReadinessCheck::severity)
        .returns(
            FermentationStartPreparationActionType.YEAST_NUTRIENT_ADDITION_PLANNED,
            FermentationStartReadinessCheck::managedByAction
        )
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("60"));
  }

  @Test
  void returnsManagedWarningWhenYanIsLowWithNutrientAdditionConfirmed() {
    final var check = policy.check(new YanFermentationStartContext(
        profile("120"),
        Set.of(FermentationStartPreparationActionType.YEAST_NUTRIENT_ADDITION_CONFIRMED)
    ));

    assertThat(check)
        .returns(FermentationStartReadinessCheckCode.YAN_LOW_MANAGED, FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.WARNING, FermentationStartReadinessCheck::severity)
        .returns(
            FermentationStartPreparationActionType.YEAST_NUTRIENT_ADDITION_CONFIRMED,
            FermentationStartReadinessCheck::managedByAction
        )
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("120"));
  }

  @Test
  void returnsWarningWhenYanIsHigh() {
    final var check = policy.check(profile("450"));

    assertThat(check)
        .returns(FermentationStartReadinessCheckCode.YAN_HIGH, FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.WARNING, FermentationStartReadinessCheck::severity)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("450"));
  }

  @Test
  void returnsInfoWhenYanIsInsideCuratedRange() {
    final var check = policy.check(profile("180"));

    assertThat(check)
        .returns(FermentationStartReadinessCheckCode.YAN_ACCEPTABLE, FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.INFO, FermentationStartReadinessCheck::severity)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("180"));
  }

  private static ChemicalProfile profile(String yan) {
    return ChemicalProfile.builder()
        .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of(yan)))
        .build();
  }

  private static ChemicalProfile profileWithoutYan() {
    return ChemicalProfile.builder()
        .ph(ExactMeasurement.of(PhValue.of("3.50")))
        .build();
  }
}
