package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.QueryFeedstockSampleSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class QueryFeedstockSampleSugarAcidityZonePointsServiceTest {

  @Test
  void filtersByBatchCapabilityZoneAndLimitInStableOrder() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var matchingBatchId = FeedstockBatchId.newId();
    final var beta = sample("Beta", matchingBatchId, profile("1.064", "5.8", "1800"), "2026-01-15T10:02:00Z");
    final var alpha = sample("Alpha", matchingBatchId, profile("1.066", "5.7", "1700"), "2026-01-15T10:01:00Z");
    final var wrongZone = sample("Wrong zone", matchingBatchId, profile("1.066", "8.2", "1700"), "2026-01-15T10:00:00Z");
    final var missingTannin = sample("Missing tannin", matchingBatchId, profile("1.066", "5.7", null), "2026-01-15T10:03:00Z");

    when(repository.findSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(wrongZone, alpha), List.of(beta, missingTannin));

    final var result = service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(
            FeedstockBatchCriteria.of(matchingBatchId),
            null,
            SugarAcidityZoneCriteria.of(SugarAcidityZone.PERFECT_APPLES),
            ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.NORTH_AMERICAN_PROFILE)
        ),
        CursorPageRequest.firstPage(1)
    ));

    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().sampleId()).isEqualTo(alpha.sampleId());
    assertThat(result.items().getFirst().plottable()).isTrue();
    assertThat(result.items().getFirst().assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void returnsNextPageUsingCursorWithoutDuplicatingSamples() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batchId = FeedstockBatchId.newId();
    final var first = sample("First", batchId, profile("1.066", "5.7", null), "2026-01-15T10:00:00Z");
    final var second = sample("Second", batchId, profile("1.066", "5.7", null), "2026-01-15T10:01:00Z");
    final var third = sample("Third", batchId, profile("1.066", "5.7", null), "2026-01-15T10:02:00Z");

    when(repository.findSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(first, second, third), List.of(third));

    final var firstPage = service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(null, null, null, null),
        CursorPageRequest.firstPage(2)
    ));
    final var secondPage = service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(null, null, null, null),
        new CursorPageRequest(firstPage.nextCursor(), 2)
    ));

    assertThat(firstPage.items())
        .extracting(FeedstockSampleSugarAcidityZoneResult::sampleId)
        .containsExactly(first.sampleId(), second.sampleId());
    assertThat(firstPage.nextCursor()).isNotNull();
    assertThat(secondPage.items())
        .extracting(FeedstockSampleSugarAcidityZoneResult::sampleId)
        .containsExactly(third.sampleId());
    assertThat(secondPage.nextCursor()).isNull();
  }

  @Test
  void canReturnSamplesNotCapableForSugarAcidityPlot() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var partial = sample("Partial", FeedstockBatchId.newId(), partialProfile());

    when(repository.findSugarAcidityPointCandidates(any())).thenReturn(List.of(partial));

    final var result = service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(
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
    final var service = service(mock(FeedstockSampleQueryRepository.class));
    final var ciderMakerId = CiderMakerId.of(randomUUID());

    assertThatThrownBy(() -> service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(null, null, null, null),
        new CursorPageRequest("cursor", 10)
    )))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cursor is malformed");
  }

  @Test
  void usesSeasonAwareRepositoryWhenSeasonFilterIsPresent() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var season = FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-01"));
    final var sample = sample("Season sample", FeedstockBatchId.newId(), profile("1.066", "5.7", null));

    when(repository.findSugarAcidityPointCandidates(any())).thenReturn(List.of(sample));

    final var result = service.query(new FeedstockSampleSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockSamplePointFilters(
            null,
            season,
            null,
            null
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().sampleId()).isEqualTo(sample.sampleId());
  }

  private static QueryFeedstockSampleSugarAcidityZonePointsService service(
      FeedstockSampleQueryRepository repository
  ) {
    return new QueryFeedstockSampleSugarAcidityZonePointsService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static FeedstockSample sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile
  ) {
    return sample(name, batchId, profile, "2026-01-15T10:00:00Z");
  }

  private static FeedstockSample sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile,
      String measuredAt
  ) {
    return FeedstockSample.create(
        FeedstockSampleId.newId(),
        batchId,
        FeedstockSampleName.of(name),
        FeedstockType.APPLE_JUICE,
        profile,
        Instant.parse(measuredAt)
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
