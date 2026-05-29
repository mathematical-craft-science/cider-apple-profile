package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.QueryFeedstockMeasurementSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstantPlusSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class QueryFeedstockMeasurementSugarAcidityZonePointsServiceTest {

  @Test
  void filtersByBatchCapabilityZoneAndLimitInStableOrder() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var matchingBatchId = FeedstockBatchId.newId();
    final var beta = sample("Beta", matchingBatchId, profile("1.064", "5.8", "1800"), feedstockMeasurementInstantPlusSeconds(120));
    final var alpha = sample("Alpha", matchingBatchId, profile("1.066", "5.7", "1700"), feedstockMeasurementInstantPlusSeconds(60));
    final var wrongZone = sample("Wrong zone", matchingBatchId, profile("1.066", "8.2", "1700"), feedstockMeasurementInstant());
    final var missingTannin = sample("Missing tannin", matchingBatchId, profile("1.066", "5.7", null), feedstockMeasurementInstantPlusSeconds(180));

    when(repository.findSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(wrongZone, alpha), List.of(beta, missingTannin));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(
            FeedstockBatchCriteria.of(matchingBatchId),
            null,
            SugarAcidityZoneCriteria.of(SugarAcidityZone.PERFECT_APPLES),
            ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.NORTH_AMERICAN_PROFILE)
        ),
        CursorPageRequest.firstPage(1)
    ));

    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().measurementId()).isEqualTo(alpha.measurementId());
    assertThat(result.items().getFirst().plottable()).isTrue();
    assertThat(result.items().getFirst().assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void returnsNextPageUsingCursorWithoutDuplicatingSamples() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batchId = FeedstockBatchId.newId();
    final var first = sample("First", batchId, profile("1.066", "5.7", null), feedstockMeasurementInstant());
    final var second = sample("Second", batchId, profile("1.066", "5.7", null), feedstockMeasurementInstantPlusSeconds(60));
    final var third = sample("Third", batchId, profile("1.066", "5.7", null), feedstockMeasurementInstantPlusSeconds(120));

    when(repository.findSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(first, second, third), List.of(third));

    final var firstPage = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        CursorPageRequest.firstPage(2)
    ));
    final var secondPage = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        new CursorPageRequest(firstPage.nextCursor(), 2)
    ));

    assertThat(firstPage.items())
        .extracting(FeedstockMeasurementSugarAcidityZoneResult::measurementId)
        .containsExactly(first.measurementId(), second.measurementId());
    assertThat(firstPage.nextCursor()).isNotNull();
    assertThat(secondPage.items())
        .extracting(FeedstockMeasurementSugarAcidityZoneResult::measurementId)
        .containsExactly(third.measurementId());
    assertThat(secondPage.nextCursor()).isNull();
  }

  @Test
  void canReturnSamplesNotCapableForSugarAcidityPlot() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var partial = sample("Partial", FeedstockBatchId.newId(), partialProfile());

    when(repository.findSugarAcidityPointCandidates(any())).thenReturn(List.of(partial));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(
            null,
            null,
            null,
            ChemicalProfileCapabilityCriteria.notCapable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT)
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().plottable()).isFalse();
    assertThat(result.items().getFirst().assessment()).isNull();
    assertThat(result.items().getFirst().capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
  }

  @Test
  void rejectsMalformedCursorExplicitly() {
    final var service = service(mock(FeedstockMeasurementQueryRepository.class));
    final var ciderMakerId = CiderMakerId.of(randomUUID());

    assertThatThrownBy(() -> service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        new CursorPageRequest("cursor", 10)
    )))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cursor is malformed");
  }

  @Test
  void usesSeasonAwareRepositoryWhenSeasonFilterIsPresent() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var season = FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-01"));
    final var sample = sample("Season sample", FeedstockBatchId.newId(), profile("1.066", "5.7", null));

    when(repository.findSugarAcidityPointCandidates(any())).thenReturn(List.of(sample));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(
            null,
            season,
            null,
            null
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().measurementId()).isEqualTo(sample.measurementId());
  }

  private static QueryFeedstockMeasurementSugarAcidityZonePointsService service(
      FeedstockMeasurementQueryRepository repository
  ) {
    return new QueryFeedstockMeasurementSugarAcidityZonePointsService(
        repository,
        projector()
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

  private static FeedstockMeasurement sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile
  ) {
    return sample(name, batchId, profile, feedstockMeasurementInstant());
  }

  private static FeedstockMeasurement sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile,
      Instant measuredAt
  ) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        profile,
        measuredAt
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
