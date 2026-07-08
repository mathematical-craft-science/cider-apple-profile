package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheck;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PhFermentationStartPolicyTest {

  private final PhFermentationStartPolicy policy = PhFermentationStartPolicy.curatedCiderV1();

  @Test
  void returnsManagedWarningWhenHighPhHasSo2Plan() {
    final var check = policy.check(new PhFermentationStartContext(
        profile("4.10"),
        Set.of(FermentationStartPreparationActionType.SO2_ADDITION_PLANNED)
    ));

    assertThat(check)
        .returns("PH_VERY_HIGH_MICROBIAL_RISK_MANAGED", FermentationStartReadinessCheck::code)
        .returns(FermentationStartReadinessSeverity.WARNING, FermentationStartReadinessCheck::severity)
        .returns(
            FermentationStartPreparationActionType.SO2_ADDITION_PLANNED,
            FermentationStartReadinessCheck::managedByAction
        )
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("4.10"));
  }

  private static ChemicalProfile profile(String ph) {
    return ChemicalProfile.builder()
        .ph(ExactMeasurement.of(PhValue.of(ph)))
        .build();
  }
}
