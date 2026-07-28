package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentMatcher;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.view;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstantPlusSeconds;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockMeasurementAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.QueryFeedstockMeasurementSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.application.model.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.junit.jupiter.api.Test;


class QueryFeedstockMeasurementSugarAcidityZonePointsServiceTest {

  @Test
  void filtersByBatchCapabilityZoneAndLimitInStableOrder() {
    final var repository = mock(FeedstockMeasurementAnalysisCandidateRepository.class);
    final var service = service(repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var matchingBatchId = FeedstockBatchId.newId();
    final var beta = sample(
        "Beta",
        matchingBatchId,
        profile("1.064", "5.8", "1800"),
        feedstockMeasurementInstantPlusSeconds(120)
    );
    final var alpha = sample(
        "Alpha",
        matchingBatchId,
        profile("1.066", "5.7", "1700"),
        feedstockMeasurementInstantPlusSeconds(60)
    );
    final var wrongZone = sample(
        "Wrong zone",
        matchingBatchId,
        profile("1.066", "8.2", "1700"),
        feedstockMeasurementInstant()
    );
    final var missingTannin = sample(
        "Missing tannin",
        matchingBatchId,
        profile("1.066", "5.7", null),
        feedstockMeasurementInstantPlusSeconds(180)
    );

    when(repository.findCandidates(any()))
        .thenReturn(List.of(view(wrongZone), view(alpha)))
        .thenReturn(List.of(view(beta), view(missingTannin)));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(
            FeedstockBatchCriteria.of(matchingBatchId),
            null,
            JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.PERFECT_APPLES),
            ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.NORTH_AMERICAN_PROFILE)
        ),
        CursorPageRequest.firstPage(1)
    ));

    assertThat(result.nextPosition()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().measurementId()).isEqualTo(alpha.measurementId());
    assertThat(result.items().getFirst().evaluation().plottable()).isTrue();
    assertThat(result.items().getFirst().evaluation().assessment().zone())
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void returnsNextPageUsingCursorWithoutDuplicatingSamples() {
    final var repository = mock(FeedstockMeasurementAnalysisCandidateRepository.class);
    final var service = service(repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batchId = FeedstockBatchId.newId();
    final var first = sample("First", batchId, profile("1.066", "5.7", null), feedstockMeasurementInstant());
    final var second = sample(
        "Second",
        batchId,
        profile("1.066", "5.7", null),
        feedstockMeasurementInstantPlusSeconds(60)
    );
    final var third = sample(
        "Third",
        batchId,
        profile("1.066", "5.7", null),
        feedstockMeasurementInstantPlusSeconds(120)
    );

    when(repository.findCandidates(any()))
        .thenReturn(List.of(view(first), view(second), view(third)))
        .thenReturn(List.of(view(third)));

    final var firstPage = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(null, null, null, null),
        CursorPageRequest.firstPage(2)
    ));
    final var secondPage = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(null, null, null, null),
        new CursorPageRequest<>(firstPage.nextPosition(), 2)
    ));

    assertThat(firstPage.items())
        .extracting(FeedstockMeasurementSugarAcidityZoneResult::measurementId)
        .containsExactly(first.measurementId(), second.measurementId());
    assertThat(firstPage.nextPosition()).isNotNull();
    assertThat(secondPage.items())
        .extracting(FeedstockMeasurementSugarAcidityZoneResult::measurementId)
        .containsExactly(third.measurementId());
    assertThat(secondPage.nextPosition()).isNull();
  }

  @Test
  void canReturnSamplesNotCapableForSugarAcidityPlot() {
    final var repository = mock(FeedstockMeasurementAnalysisCandidateRepository.class);
    final var service = service(repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var partial = sample("Partial", FeedstockBatchId.newId(), partialProfile());

    when(repository.findCandidates(any())).thenReturn(List.of(view(partial)));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(
            null,
            null,
            null,
            ChemicalProfileCapabilityCriteria.notCapable(
                ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH
            )
        ),
        CursorPageRequest.firstPage()
    ));

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().evaluation().plottable()).isFalse();
    assertThat(result.items().getFirst().evaluation().assessment()).isNull();
    assertThat(result.items().getFirst().evaluation().capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH);
  }

  @Test
  void usesSeasonAwareRepositoryWhenSeasonFilterIsPresent() {
    final var repository = mock(FeedstockMeasurementAnalysisCandidateRepository.class);
    final var service = service(repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var season = FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-01"));
    final var sample = sample("Season sample", FeedstockBatchId.newId(), profile("1.066", "5.7", null));

    when(repository.findCandidates(any())).thenReturn(List.of(view(sample)));

    final var result = service.query(new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(
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
      FeedstockMeasurementAnalysisCandidateRepository repository
  ) {
    return new QueryFeedstockMeasurementSugarAcidityZonePointsService(
        repository,
        mock(FeedstockAccessControl.class),
        projector(),
        chemicalProfileAssessmentMatcher()
    );
  }

  private static ChemicalProfileAssessmentAdapter projector() {
    return chemicalProfileAssessmentAdapter();
  }

  private static FeedstockMeasurementFixture sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile
  ) {
    return sample(name, batchId, profile, feedstockMeasurementInstant());
  }

  private static FeedstockMeasurementFixture sample(
      String name,
      FeedstockBatchId batchId,
      ChemicalProfile profile,
      Instant measuredAt
  ) {
    return FeedstockMeasurementFixture.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        profile,
        measuredAt,
        feedstockMeasurementRecordedAt()
    );
  }

  private static ChemicalProfile profile(
      String sg,
      String acidity,
      String tannin
  ) {
    final var builder = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(sg)
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)));

    if (tannin != null) {
      builder.tannin(PointCharacteristicValue.of(TanninValue.of(tannin)));
    }

    return builder.build();
  }

  private static ChemicalProfile partialProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
  }
}
