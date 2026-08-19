package org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.AppleVarietyAnalysisProfileNotFoundException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.GetAppleVarietyReferenceProfileUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogAppleVarietyAnalysisProfileAdapterTest {

  @Test
  void translatesMissingCatalogProfileToAnalysisLanguage() {
    final var varietyId = AppleVarietyId.newId();
    final var catalogQuery = mock(GetAppleVarietyReferenceProfileUseCase.class);
    when(catalogQuery.get(varietyId)).thenThrow(new AppleVarietyNotFoundException(varietyId));

    assertThatThrownBy(() -> new CatalogAppleVarietyAnalysisProfileAdapter(catalogQuery).get(varietyId))
        .isInstanceOf(AppleVarietyAnalysisProfileNotFoundException.class)
        .satisfies(exception -> assertThat((AppleVarietyAnalysisProfileNotFoundException) exception)
            .returns(varietyId, AppleVarietyAnalysisProfileNotFoundException::varietyId));
  }
}
