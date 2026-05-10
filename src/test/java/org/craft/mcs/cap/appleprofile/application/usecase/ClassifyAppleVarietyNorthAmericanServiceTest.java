package org.craft.mcs.cap.appleprofile.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.usecase.ClassifyAppleVarietyNorthAmericanService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class ClassifyAppleVarietyNorthAmericanServiceTest {

  @Test
  void rejectsIncompleteProfileBeforeCallingCalculator() {
    final var repository = mock(AppleVarietyRepository.class);
    final var calculator = mock(NorthAmericanProfileCalculator.class);
    final var service = new ClassifyAppleVarietyNorthAmericanService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        calculator
    );
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    assertThatThrownBy(() -> service.classify(variety.varietyId()))
        .isInstanceOfSatisfying(
            ChemicalProfileCapabilityNotMetException.class,
            exception -> {
              assertThat(exception.assessment().capability())
                  .isEqualTo(ChemicalProfileCapability.NORTH_AMERICAN_PROFILE);
              assertThat(exception.assessment().missingAxes())
                  .containsExactlyInAnyOrder(ChemicalAxis.ACIDITY, ChemicalAxis.TANNINITY);
            }
        );
    verifyNoInteractions(calculator);
  }
}
