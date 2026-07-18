package org.craft.mcs.cap.appleprofile.application.sugaracidity.usecase;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out.AppleVarietySugarAcidityPointCandidateView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase.QueryAppleVarietySugarAcidityZonePointsService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class QueryAppleVarietySugarAcidityZonePointsServiceTest {

  @Test
  void filtersByTextCapabilityZoneAndLimitInStableOrder() {
    final var repository = mock(AppleVarietySugarAcidityPointCandidateRepository.class);
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
        new AppleVarietyPointFilters(
            new TextSearchCriteria("kingston"),
            SugarAcidityZoneCriteria.of(SugarAcidityZone.PERFECT_APPLES),
            ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.NORTH_AMERICAN_PROFILE)
        ),
        CursorPageRequest.firstPage(1)
    ));

    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().varietyId()).isEqualTo(alpha.varietyId());
    assertThat(result.items().getFirst().evaluation().plottable()).isTrue();
    assertThat(result.items().getFirst().evaluation().assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
    assertThat(result.items().getFirst().potentialAbv())
        .isNotNull()
        .satisfies(prediction -> assertThat(prediction.potentialAbv().percent())
            .isEqualByComparingTo("8.663"));
  }

  @Test
  void returnsNextPageUsingCursorWithoutDuplicatingVarieties() {
    final var repository = mock(AppleVarietySugarAcidityPointCandidateRepository.class);
    final var service = service(repository);
    final var first = variety("A First", profile("1.066", "5.7", null));
    final var second = variety("B Second", profile("1.066", "5.7", null));
    final var third = variety("C Third", profile("1.066", "5.7", null));

    when(repository.findCandidates(any()))
        .thenReturn(List.of(candidate(first), candidate(second), candidate(third)))
        .thenReturn(List.of(candidate(third)));

    final var firstPage = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(null, null, null),
        CursorPageRequest.firstPage(2)
    ));
    final var secondPage = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(null, null, null),
        new CursorPageRequest(firstPage.nextCursor(), 2)
    ));

    assertThat(firstPage.items())
        .extracting(AppleVarietySugarAcidityZoneResult::varietyId)
        .containsExactly(first.varietyId(), second.varietyId());
    assertThat(firstPage.nextCursor()).isNotNull();
    assertThat(secondPage.items())
        .extracting(AppleVarietySugarAcidityZoneResult::varietyId)
        .containsExactly(third.varietyId());
    assertThat(secondPage.nextCursor()).isNull();
  }

  @Test
  void canReturnVarietiesNotCapableForSugarAcidityPlot() {
    final var repository = mock(AppleVarietySugarAcidityPointCandidateRepository.class);
    final var service = service(repository);
    final var partial = variety("Partial", partialProfile());

    when(repository.findCandidates(any())).thenReturn(List.of(candidate(partial)));

    final var result = service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(
            null,
            null,
            ChemicalProfileCapabilityCriteria.notCapable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT)
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().evaluation().plottable()).isFalse();
    assertThat(result.items().getFirst().evaluation().assessment()).isNull();
    assertThat(result.items().getFirst().potentialAbv()).isNotNull();
  }

  @Test
  void rejectsMalformedCursorExplicitly() {
    final var service = service(mock(AppleVarietySugarAcidityPointCandidateRepository.class));

    assertThatThrownBy(() -> service.query(new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(null, null, null),
        new CursorPageRequest("cursor", 10)
    )))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cursor is malformed");
  }

  private static QueryAppleVarietySugarAcidityZonePointsService service(
      AppleVarietySugarAcidityPointCandidateRepository repository
  ) {
    return new QueryAppleVarietySugarAcidityZonePointsService(
        repository,
        projector(),
        predictor()
    );
  }

  private static SugarAcidityProfileProjector projector() {
    return new SugarAcidityProfileProjector(
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static AppleVariety variety(
      String name,
      ChemicalProfile profile
  ) {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of(name),
        profile
    );
  }

  private static AppleVarietySugarAcidityPointCandidateView candidate(AppleVariety variety) {
    return new AppleVarietySugarAcidityPointCandidateView(
        variety.varietyId(),
        variety.name(),
        variety.chemicalProfile()
    );
  }

  private static ChemicalProfile profile(
      String sg,
      String acidity,
      String tannin
  ) {
    final var builder = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(sg)
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)));

    if (tannin != null) {
      builder.tanninity(ExactMeasurement.of(TanninityValue.of(tannin)));
    }

    return builder.build();
  }

  private static ChemicalProfile partialProfile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
  }
}
