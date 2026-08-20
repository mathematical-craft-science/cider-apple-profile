package org.craft.mcs.cap.appleprofile.analysis.application.abv.usecase;

import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.AppleVarietyPotentialAbvUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.in.AppleVarietyPotentialAbvPrediction;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.usecase.PredictAppleVarietyPotentialAbvService;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.applePotentialAbvAnalyzer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PredictAppleVarietyPotentialAbvServiceTest {

  @Test
  void predictsPotentialAbvFromReferenceSpecificGravity() {
    final var profileProvider = mock(AppleVarietyAnalysisProfileProvider.class);
    final var service = new PredictAppleVarietyPotentialAbvService(profileProvider, applePotentialAbvAnalyzer());
    final var variety = variety(
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
            null,
            null,
            null
        )
    );
    when(profileProvider.get(variety.varietyId())).thenReturn(variety);

    final var result = service.predict(variety.varietyId());

    assertThat(result)
        .returns(variety.varietyId(), AppleVarietyPotentialAbvPrediction::varietyId)
        .returns(variety.name(), AppleVarietyPotentialAbvPrediction::varietyName)
        .returns(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
            AppleVarietyPotentialAbvPrediction::originalGravity
        );
    assertThat(result.prediction().potentialAbv().percent())
        .isEqualByComparingTo("6.825");
  }

  @Test
  void rejectsReferenceProfileWithoutSpecificGravity() {
    final var profileProvider = mock(AppleVarietyAnalysisProfileProvider.class);
    final var service = new PredictAppleVarietyPotentialAbvService(profileProvider, applePotentialAbvAnalyzer());
    final var variety = variety(
        new AppleVarietyReferenceProfile(
            null,
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.0")),
            null,
            null
        )
    );
    when(profileProvider.get(variety.varietyId())).thenReturn(variety);

    assertThatThrownBy(() -> service.predict(variety.varietyId()))
        .isInstanceOf(AppleVarietyPotentialAbvUnavailableException.class)
        .hasMessageContaining(variety.varietyId().value().toString());
  }

  private static @NonNull AppleVarietyAnalysisProfile variety(
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    return new AppleVarietyAnalysisProfile(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Test variety"),
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity(),
        referenceProfile.ph(),
        referenceProfile.tannin()
    );
  }
}
