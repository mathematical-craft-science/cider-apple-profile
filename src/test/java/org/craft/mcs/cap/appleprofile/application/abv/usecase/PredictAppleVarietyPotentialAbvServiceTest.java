package org.craft.mcs.cap.appleprofile.application.abv.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.abv.AppleVarietyPotentialAbvUnavailableException;
import org.craft.mcs.cap.core.appleprofile.application.abv.port.in.AppleVarietyPotentialAbvPrediction;
import org.craft.mcs.cap.core.appleprofile.application.abv.usecase.PredictAppleVarietyPotentialAbvService;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PredictAppleVarietyPotentialAbvServiceTest {

  @Test
  void predictsPotentialAbvFromReferenceSpecificGravity() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new PredictAppleVarietyPotentialAbvService(repository, predictor());
    final var variety = variety(
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
            null,
            null,
            null
        )
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

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
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new PredictAppleVarietyPotentialAbvService(repository, predictor());
    final var variety = variety(
        new AppleVarietyReferenceProfile(
            null,
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.0")),
            null,
            null
        )
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    assertThatThrownBy(() -> service.predict(variety.varietyId()))
        .isInstanceOf(AppleVarietyPotentialAbvUnavailableException.class)
        .hasMessageContaining(variety.varietyId().value().toString());
  }

  private static @NonNull AppleVariety variety(
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Test variety"),
        referenceProfile
    );
  }

  private static @NonNull PotentialAbvPredictor predictor() {
    return new PotentialAbvPredictor(
        FixedFinalGravityPredictionStrategy.dryCiderDefault(),
        new StandardGravityDropAbvPredictionStrategy()
    );
  }
}
