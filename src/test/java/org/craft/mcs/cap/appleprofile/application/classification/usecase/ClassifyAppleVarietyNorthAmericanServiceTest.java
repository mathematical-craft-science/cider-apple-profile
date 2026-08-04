package org.craft.mcs.cap.appleprofile.application.classification.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.classification.AppleVarietyNorthAmericanProfileUnavailableException;
import org.craft.mcs.cap.core.appleprofile.application.classification.usecase.ClassifyAppleVarietyNorthAmericanService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class ClassifyAppleVarietyNorthAmericanServiceTest {

  @Test
  void rejectsIncompleteProfileBeforeCallingCalculator() {
    final var repository = mock(AppleVarietyRepository.class);
    final var calculator = mock(NorthAmericanProfileCalculator.class);
    final var service = new ClassifyAppleVarietyNorthAmericanService(repository, calculator);
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
            null,
            null,
            null
        )
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    assertThatThrownBy(() -> service.classify(variety.varietyId()))
        .isInstanceOf(AppleVarietyNorthAmericanProfileUnavailableException.class)
        .hasMessageContaining("Specific Gravity, Titratable Acidity and tannin");
    verifyNoInteractions(calculator);
  }
}
