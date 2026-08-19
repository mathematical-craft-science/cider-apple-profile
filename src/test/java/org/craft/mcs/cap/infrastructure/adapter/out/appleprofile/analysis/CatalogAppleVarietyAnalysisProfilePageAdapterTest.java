package org.craft.mcs.cap.infrastructure.adapter.out.appleprofile.analysis;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisPageQuery;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.QueryAppleVarietyReferenceProfilePageUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CatalogAppleVarietyAnalysisProfilePageAdapterTest {

  @Test
  void mapsAnalysisQueryAndCatalogPublishedViewAcrossAcl() {
    final var catalogQuery = mock(QueryAppleVarietyReferenceProfilePageUseCase.class);
    final var adapter = new CatalogAppleVarietyAnalysisProfilePageAdapter(catalogQuery);
    final var varietyId = AppleVarietyId.newId();
    final var profile = new AppleVarietyReferenceProfile(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.060")),
        null,
        null,
        null
    );
    when(catalogQuery.query(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
        new AppleVarietyReferenceProfileView(
            varietyId,
            "Kingston Black",
            profile.specificGravity(),
            profile.titratableAcidity(),
            profile.ph(),
            profile.tannin()
        )
    ));
    final var nameContains = new TextSearchCriteria("kingston");
    final var position = new AppleVarietyAnalysisPagePosition("Brown Snout", AppleVarietyId.newId().value());

    final var result = adapter.findProfiles(new AppleVarietyAnalysisPageQuery(
        nameContains,
        position,
        25
    ));

    assertThat(result)
        .singleElement()
        .returns(varietyId, AppleVarietyAnalysisProfile::varietyId)
        .returns(AppleVarietyAnalysisName.of("Kingston Black"), AppleVarietyAnalysisProfile::name)
        .returns(profile.specificGravity(), AppleVarietyAnalysisProfile::specificGravity);
    final var queryCaptor = ArgumentCaptor.forClass(AppleVarietyReferenceProfilePageQuery.class);
    verify(catalogQuery).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue())
        .returns(nameContains, AppleVarietyReferenceProfilePageQuery::nameContains)
        .returns(25, AppleVarietyReferenceProfilePageQuery::limit)
        .satisfies(query -> assertThat(query.after())
            .isNotNull()
            .returns(position.name(), AppleVarietyCatalogPagePosition::name)
            .returns(position.varietyId(), AppleVarietyCatalogPagePosition::varietyId));
  }
}
