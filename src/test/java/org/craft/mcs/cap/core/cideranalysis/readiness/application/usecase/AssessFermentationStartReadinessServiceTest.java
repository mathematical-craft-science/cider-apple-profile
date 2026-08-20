package org.craft.mcs.cap.core.cideranalysis.readiness.application.usecase;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartPreparationAction;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartReadinessView;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.FermentationStartReadinessCommand;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AssessFermentationStartReadinessServiceTest {

  private final AssessFermentationStartReadinessService service =
      new AssessFermentationStartReadinessService(new FermentationStartReadinessAssessor());

  @Test
  void mapsManagedDomainAssessmentToPublishedContract() {
    final var result = service.assess(new FermentationStartReadinessCommand(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
        PointCharacteristicValue.of(PhValue.of("4.10")),
        PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180")),
        Set.of(FermentationStartPreparationAction.SO2_ADDITION_PLANNED)
    ));

    assertThat(result)
        .returns(FermentationStartReadinessView.Status.NEEDS_ATTENTION, FermentationStartReadinessView::status);
    assertThat(result.checks())
        .filteredOn(
            FermentationStartReadinessView.Check::factor,
            FermentationStartReadinessView.Factor.PH
        )
        .singleElement()
        .returns(
            FermentationStartReadinessView.Code.PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
            FermentationStartReadinessView.Check::code
        )
        .returns(
            FermentationStartPreparationAction.SO2_ADDITION_PLANNED,
            FermentationStartReadinessView.Check::managedByAction
        );
  }
}
