package org.craft.mcs.cap.appleprofile.application.sugaracidity.usecase;

import static java.util.Objects.requireNonNull;
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
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase.QueryAppleVarietySugarAcidityZonePointsService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.service.AppleVarietyGraphCandidateMatcher;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanAcidityLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
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

    assertThat(result)
        .matches(page -> page.nextPosition() != null)
        .satisfies(page -> assertThat(page.items()).hasSize(1));
    assertThat(result.items().getFirst())
        .returns(alpha.varietyId(), AppleVarietyGraphAnalysisView::varietyId)
        .returns(true, view -> view.graphEvaluation().plottable())
        .returns(
            JolicoeurSugarAcidityZone.PERFECT_APPLES,
            view -> requireNonNull(view.graphEvaluation().zoneAssessment()).zone()
        )
        .returns(alpha.referenceProfile().tannin(), AppleVarietyGraphAnalysisView::tannin)
        .satisfies(QueryAppleVarietySugarAcidityZonePointsServiceTest::assertNorthAmericanProfile)
        .satisfies(view -> assertThat(view.potentialAbv())
            .isNotNull()
            .satisfies(prediction -> assertThat(prediction.potentialAbv().percent())
                .isEqualByComparingTo("8.663")));
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

    assertThat(firstPage)
        .matches(page -> page.nextPosition() != null)
        .satisfies(page -> assertThat(page.items())
            .extracting(AppleVarietyGraphAnalysisView::varietyId)
            .containsExactly(first.varietyId(), second.varietyId()));
    assertThat(secondPage)
        .returns(null, CursorPage::nextPosition)
        .satisfies(page -> assertThat(page.items())
            .extracting(AppleVarietyGraphAnalysisView::varietyId)
            .containsExactly(third.varietyId()));
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

    assertThat(result.items())
        .singleElement()
        .returns(false, view -> view.graphEvaluation().plottable())
        .returns(null, view -> view.graphEvaluation().zoneAssessment())
        .matches(view -> view.potentialAbv() != null);
  }

  private static void assertNorthAmericanProfile(
      AppleVarietyGraphAnalysisView view
  ) {
    assertThat(view.northAmericanAssessment())
        .isNotNull()
        .returns(NorthAmericanSugarLevel.HIGH, AppleVarietyNorthAmericanAssessmentView::sugar)
        .returns(NorthAmericanAcidityLevel.MEDIUM, AppleVarietyNorthAmericanAssessmentView::acidity)
        .returns(NorthAmericanTanninLevel.MEDIUM, AppleVarietyNorthAmericanAssessmentView::tannin);
  }

  private static QueryAppleVarietySugarAcidityZonePointsService service(
      AppleVarietyAnalysisCandidateQuery repository
  ) {
    return new QueryAppleVarietySugarAcidityZonePointsService(
        repository,
        new AppleVarietyGraphCandidateMatcher(),
        new AppleVarietyGraphAnalysisProjector(
            graphEvaluator(),
            northAmericanProfileCalculator(),
            predictor()
        )
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
