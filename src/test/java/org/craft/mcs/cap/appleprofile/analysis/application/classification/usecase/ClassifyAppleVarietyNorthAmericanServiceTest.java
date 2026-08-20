package org.craft.mcs.cap.appleprofile.analysis.application.classification.usecase;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.AppleVarietyNorthAmericanProfileUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.usecase.ClassifyAppleVarietyNorthAmericanService;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out.AppleCiderClassificationAnalyzer;
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
    final var profileProvider = mock(AppleVarietyAnalysisProfileProvider.class);
    final var analyzer = mock(AppleCiderClassificationAnalyzer.class);
    final var service = new ClassifyAppleVarietyNorthAmericanService(profileProvider, analyzer);
    final var variety = new AppleVarietyAnalysisProfile(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null,
        null
    );
    when(profileProvider.get(variety.varietyId())).thenReturn(variety);

    assertThatThrownBy(() -> service.classify(variety.varietyId()))
        .isInstanceOf(AppleVarietyNorthAmericanProfileUnavailableException.class)
        .hasMessageContaining("Specific Gravity, Titratable Acidity and tannin");
    verifyNoInteractions(analyzer);
  }
}
