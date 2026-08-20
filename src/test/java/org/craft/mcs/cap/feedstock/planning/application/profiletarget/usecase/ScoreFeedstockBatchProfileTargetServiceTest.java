package org.craft.mcs.cap.feedstock.planning.application.profiletarget.usecase;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures
    .feedstockPlanningGraphAnalyzer;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageProvider;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidateSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.port.out.FeedstockPlanningCandidateProvider;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView.Role;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase.FeedstockBatchProfileTargetScorableCandidateProvider;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase.QueryFeedstockBatchProfileTargetCandidatesService;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase.ScoreFeedstockBatchProfileTargetService;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.usecase.ScoreTargetFitService;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit.FeedstockTargetFitAnalysisAdapter;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


class ScoreFeedstockBatchProfileTargetServiceTest {

  @Test
  void assessesExcludedLowSugarBoundaryThroughTheCompleteCandidatePipeline() {
    final var repository = mock(FeedstockCurrentProfilePageProvider.class);
    final var scoreService = service(repository);
    final var service = new QueryFeedstockBatchProfileTargetCandidatesService(scoreService);
    final var boundaryBatch = sample("Excluded low-sugar boundary", profile("1.045", "7.6"));
    when(repository.findProfiles(any())).thenReturn(List.of(currentProfile(boundaryBatch)));

    final var result = service.query(new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(newUuid()),
        new FeedstockPlanningProfileFilters(null, null),
        null,
        AnalysisScenario.BLENDING_PLANNING,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.LOW_SUGAR_APPLES
        ),
        null,
        ObjectiveWeights.balanced(),
        200
    ));

    assertThat(result.candidates())
        .singleElement()
        .extracting(
            candidate -> candidate.distance().insideSugarAcidityTarget(),
            candidate -> candidate.distance().missingAxes().isEmpty(),
            candidate -> requireNonNull(candidate.distance().value()).signum(),
            candidate -> candidate.contributionRole().roles()
        )
        .containsExactly(
            false,
            true,
            0,
            List.of(Role.SUGAR_CONTRIBUTOR)
        );
  }

  @Test
  void scoresLatestMeasurementCandidatesPerBatchAndSortsByDistance() {
    final var repository = mock(FeedstockCurrentProfilePageProvider.class);
    final var service = service(repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var near = sample("Near", profile("1.070", "6.0"));
    final var far = sample("Far", profile("1.040", "9.0"));

    when(repository.findProfiles(any()))
        .thenReturn(List.of(currentProfile(far), currentProfile(near)));

    final var result = service.score(new FeedstockBatchProfileTargetQuery(
        workspaceId,
        new FeedstockPlanningProfileFilters(null, null),
        null,
        AnalysisScenario.BLENDING_PLANNING,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        ObjectiveWeights.balanced(),
        50
    ));

    assertThat(result.scores())
        .extracting(FeedstockBatchProfileTargetScore::batchId)
        .containsExactly(near.batchId(), far.batchId());
    assertThat(result.scores())
        .extracting(score -> score.profileEvidence().getFirst().evidenceReference().measurementSetId())
        .containsExactly(near.measurementId().value(), far.measurementId().value());
    assertThat(result.scores().getFirst().distance().value()).isEqualByComparingTo("0");

    final var query = ArgumentCaptor.forClass(FeedstockCurrentProfilePageQuery.class);
    verify(repository).findProfiles(query.capture());
    assertThat(query.getValue().workspaceId()).isEqualTo(workspaceId);
    assertThat(query.getValue().limit()).isEqualTo(50 + QueryLimits.PAGE_LOOKAHEAD);
  }

  @Test
  void filtersCandidatesBySourceZoneBeforeTargetScoring() {
    final var repository = mock(FeedstockCurrentProfilePageProvider.class);
    final var service = service(repository);
    final var perfect = sample("Perfect source", profile("1.070", "6.0"));
    final var lowSugar = sample("Low-sugar source", profile("1.040", "7.6"));
    when(repository.findProfiles(any()))
        .thenReturn(List.of(currentProfile(lowSugar), currentProfile(perfect)));

    final var result = service.score(new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(newUuid()),
        new FeedstockPlanningProfileFilters(null, null),
        new FeedstockPlanningGraphCriteria(Set.of(Zone.PERFECT_APPLES), null),
        AnalysisScenario.BLENDING_PLANNING,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        ObjectiveWeights.balanced(),
        50
    ));

    assertThat(result.scores())
        .singleElement()
        .returns(perfect.batchId(), FeedstockBatchProfileTargetScore::batchId);
  }

  @Test
  void continuesReadingCandidatesAfterAFullChunkIsRejectedByApplicationFilters() {
    final var repository = mock(FeedstockCurrentProfilePageProvider.class);
    final var service = service(repository);
    final var rejectedWithoutAcidity = sample("Missing TA", profileWithSugarOnly("1.050"));
    final var rejectedWithoutSugar = sample("Missing SG", profileWithAcidityOnly("6.0"));
    final var accepted = sample("Scorable", profile("1.070", "6.0"));
    final var firstChunk = List.of(currentProfile(rejectedWithoutAcidity), currentProfile(rejectedWithoutSugar));
    final var secondChunk = List.of(currentProfile(accepted));

    when(repository.findProfiles(any()))
        .thenReturn(firstChunk)
        .thenReturn(secondChunk);

    final var result = service.score(new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(newUuid()),
        new FeedstockPlanningProfileFilters(null, null),
        null,
        AnalysisScenario.BLENDING_PLANNING,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        ObjectiveWeights.balanced(),
        1
    ));

    assertThat(result.scores())
        .singleElement()
        .satisfies(score -> assertThat(score.profileEvidence())
            .extracting(item -> item.evidenceReference().measurementSetId())
            .containsOnly(accepted.measurementId().value()));

    final var queryCaptor = ArgumentCaptor.forClass(FeedstockCurrentProfilePageQuery.class);
    verify(repository, times(2)).findProfiles(queryCaptor.capture());
    assertThat(queryCaptor.getAllValues())
        .extracting(FeedstockCurrentProfilePageQuery::limit)
        .containsOnly(1 + QueryLimits.PAGE_LOOKAHEAD);
    assertThat(queryCaptor.getAllValues().getFirst().after()).isNull();
    assertThat(queryCaptor.getAllValues().getLast().after()).isNotNull();
  }

  private static ScoreFeedstockBatchProfileTargetService service(
      FeedstockCurrentProfilePageProvider repository
  ) {
    final var geometryProfile = BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    return new ScoreFeedstockBatchProfileTargetService(
        new FeedstockBatchProfileTargetScorableCandidateProvider(
            candidateProvider(repository),
            feedstockPlanningGraphAnalyzer(geometryProfile)
        ),
        new FeedstockTargetFitAnalysisAdapter(new ScoreTargetFitService(
            new ZoneGeometrySugarAcidityTargetResolver(geometryProfile),
            new BuiltInProfileAxisNormalizerSelector(),
            new BuiltInProfileTargetObjectiveFunctionFactory(),
            new TargetAwareCompositionContributionRoleAssessor()
        )),
        mock(FeedstockPlanningAccessControl.class)
    );
  }

  private static FeedstockPlanningCandidateProvider candidateProvider(
      FeedstockCurrentProfilePageProvider repository
  ) {
    return query -> repository.findProfiles(new FeedstockCurrentProfilePageQuery(
            query.workspaceId(),
            query.batch() == null ? null : new FeedstockBatchCriteria(query.batch().batchIds()),
            query.season(),
            query.after() == null
                ? null
                : new FeedstockCurrentProfilePagePosition(
                    query.after().measuredAt(),
                    query.after().measurementSetId()
                ),
            query.limit()
        )).stream()
        .map(profile -> new FeedstockPlanningCandidateSnapshot(
            profile,
            VolumeValue.ofLiters("100")
        ))
        .toList();
  }

  private static FeedstockMeasurementFixture sample(
      String name,
      ProfileCharacteristicsFixture profile
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        FeedstockBatchId.newId(),
        MeasurementSetName.of(name),
        profile,
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static ProfileCharacteristicsFixture profile(
      String sg,
      String acidity
  ) {
    return ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of(sg)))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)))
        .build();
  }

  private static ProfileCharacteristicsFixture profileWithSugarOnly(String sg) {
    return ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of(sg)))
        .build();
  }

  private static ProfileCharacteristicsFixture profileWithAcidityOnly(String acidity) {
    return ProfileCharacteristicsFixture.builder()
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)))
        .build();
  }
}
