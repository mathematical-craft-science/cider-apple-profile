package org.craft.mcs.cap.appleprofile.catalog.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.catalog.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.catalog.application.usecase.GetAppleVarietyReferenceProfileService;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class GetAppleVarietyReferenceProfileServiceTest {

  @Test
  void publishesReferenceProfileWithoutExposingAggregate() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new GetAppleVarietyReferenceProfileService(repository);
    final var variety = variety();
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    assertThat(service.get(variety.varietyId()))
        .returns(variety.varietyId(), AppleVarietyReferenceProfileView::varietyId)
        .returns(variety.name().value(), AppleVarietyReferenceProfileView::name)
        .returns(variety.referenceProfile().specificGravity(), AppleVarietyReferenceProfileView::specificGravity)
        .returns(
            variety.referenceProfile().titratableAcidity(),
            AppleVarietyReferenceProfileView::titratableAcidity
        )
        .returns(variety.referenceProfile().ph(), AppleVarietyReferenceProfileView::ph)
        .returns(variety.referenceProfile().tannin(), AppleVarietyReferenceProfileView::tannin);
  }

  @Test
  void preservesCatalogNotFoundSemantics() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new GetAppleVarietyReferenceProfileService(repository);
    final var varietyId = AppleVarietyId.newId();
    when(repository.findById(varietyId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(varietyId))
        .isInstanceOf(AppleVarietyNotFoundException.class)
        .hasMessageContaining(varietyId.value().toString());
  }

  private static AppleVariety variety() {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.060")),
            null,
            null,
            null
        )
    );
  }
}
