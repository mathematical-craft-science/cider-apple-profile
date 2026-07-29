package org.craft.mcs.cap.appleprofile.application.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.shared.testing.AppleVarietyReferenceProfiles.from;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyAnalysisCandidateView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase.QueryAppleVarietySugarAcidityZonePointsService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.service.AppleVarietyGraphCandidateMatcher;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;


class QueryAppleVarietySugarAcidityZonePointsServiceTest {

  @Test
  void filtersByTextCapabilityZoneAndLimitInStableOrder() {
    final var repository = mock(AppleVarietyAnalysisCandidateQuery.class);
    final var service = service(repository);
    final var beta = variety("Kingston Beta", profile("1.064", "5.8", "1800"));
    final var alpha = variety("Kingston Alpha", profile("1.066", "5.7", "1700"));
    final var wrongZone = variety("Kingston Sharp", profile("1.066", "8.2", "1700"));

    when(repository.findCandidates(any())).thenReturn(List.of(
        candidate(alpha),
        candidate(beta),
        candidate(wrongZone)
    ));

    final var result = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(
            new TextSearchCriteria("kingston"),
            JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.PERFECT_APPLES),
            ProfileAnalysisCapabilityCriteria.capable(ProfileAnalysisCapability.NORTH_AMERICAN_PROFILE)
        ),
        CursorPageRequest.firstPage(1)
    ));

    assertThat(result.nextPosition()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().varietyId()).isEqualTo(alpha.varietyId());
    assertThat(result.items().getFirst().graphEvaluation().plottable()).isTrue();
    assertThat(result.items().getFirst().graphEvaluation().zoneAssessment().zone())
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
    assertThat(result.items().getFirst().potentialAbv())
        .isNotNull()
        .satisfies(prediction -> assertThat(prediction.potentialAbv().percent())
            .isEqualByComparingTo("8.663"));
  }

  @Test
  void returnsNextPageUsingCursorWithoutDuplicatingVarieties() {
    final var repository = mock(AppleVarietyAnalysisCandidateQuery.class);
    final var service = service(repository);
    final var first = variety("A First", profile("1.066", "5.7", null));
    final var second = variety("B Second", profile("1.066", "5.7", null));
    final var third = variety("C Third", profile("1.066", "5.7", null));

    when(repository.findCandidates(any()))
        .thenReturn(List.of(candidate(first), candidate(second), candidate(third)))
        .thenReturn(List.of(candidate(third)));

    final var firstPage = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(null, null, null),
        CursorPageRequest.firstPage(2)
    ));
    final var secondPage = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(null, null, null),
        new CursorPageRequest<>(firstPage.nextPosition(), 2)
    ));

    assertThat(firstPage.items())
        .extracting(AppleVarietySugarAcidityZoneResult::varietyId)
        .containsExactly(first.varietyId(), second.varietyId());
    assertThat(firstPage.nextPosition()).isNotNull();
    assertThat(secondPage.items())
        .extracting(AppleVarietySugarAcidityZoneResult::varietyId)
        .containsExactly(third.varietyId());
    assertThat(secondPage.nextPosition()).isNull();
  }

  @Test
  void canReturnVarietiesNotCapableForSugarAcidityPlot() {
    final var repository = mock(AppleVarietyAnalysisCandidateQuery.class);
    final var service = service(repository);
    final var partial = variety("Partial", partialProfile());

    when(repository.findCandidates(any())).thenReturn(List.of(candidate(partial)));

    final var result = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(
            null,
            null,
            ProfileAnalysisCapabilityCriteria.notCapable(
                ProfileAnalysisCapability.SUGAR_ACIDITY_GRAPH
            )
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().graphEvaluation().plottable()).isFalse();
    assertThat(result.items().getFirst().graphEvaluation().zoneAssessment()).isNull();
    assertThat(result.items().getFirst().potentialAbv()).isNotNull();
  }

  private static QueryAppleVarietySugarAcidityZonePointsService service(
      AppleVarietyAnalysisCandidateQuery repository
  ) {
    return new QueryAppleVarietySugarAcidityZonePointsService(
        repository,
        graphEvaluator(),
        northAmericanProfileCalculator(),
        new AppleVarietyGraphCandidateMatcher(),
        predictor()
    );
  }

  private static AppleVariety variety(
      String name,
      ProfileCharacteristicsFixture profile
  ) {
    return variety(name, from(profile));
  }

  private static AppleVariety variety(
      String name,
      AppleVarietyReferenceProfile profile
  ) {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of(name),
        profile
    );
  }

  private static AppleVarietyAnalysisCandidateView candidate(AppleVariety variety) {
    return new AppleVarietyAnalysisCandidateView(
        variety.varietyId(),
        variety.name(),
        variety.referenceProfile()
    );
  }

  private static ProfileCharacteristicsFixture profile(
      String sg,
      String acidity,
      String tannin
  ) {
    final var builder = ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of(sg)))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)));

    if (tannin != null) {
      builder.tannin(PointCharacteristicValue.of(TanninValue.of(tannin)));
    }

    return builder.build();
  }

  private static AppleVarietyReferenceProfile partialProfile() {
    return new AppleVarietyReferenceProfile(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null,
        null
    );
  }
}
