package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.usecase.BuiltInProfileTargetScoringPurposeNormalizerPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.usecase.ScoreProfileTargetDistanceCandidatesService;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.usecase.ScoreFeedstockBatchProfileTargetService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ScoreFeedstockBatchProfileTargetServiceTest {

  @Test
  void scoresLatestMeasurementCandidatesPerBatchAndSortsByDistance() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var near = sample("Near", profile("1.070", "6.0"));
    final var far = sample("Far", profile("1.040", "9.0"));

    when(repository.findLatestSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(far, near));

    final var result = service.score(new ScoreFeedstockBatchProfileTargetCommand(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        new PredefinedFeedstockProfileTargetSelection(SugarAcidityZone.PERFECT_APPLES),
        null,
        ProfileTargetObjectiveWeights.balanced(),
        50
    ));

    assertThat(result.scores())
        .extracting(FeedstockBatchProfileTargetScore::batchId)
        .containsExactly(near.batchId(), far.batchId());
    assertThat(result.scores())
        .extracting(FeedstockBatchProfileTargetScore::currentMeasurementId)
        .containsExactly(near.measurementId(), far.measurementId());
    assertThat(result.scores().getFirst().distance().value()).isEqualByComparingTo("0");

    final var query = ArgumentCaptor.forClass(LatestFeedstockMeasurementSugarAcidityPointCandidateQuery.class);
    verify(repository).findLatestSugarAcidityPointCandidates(query.capture());
    verify(repository, never()).findSugarAcidityPointCandidates(any());
    assertThat(query.getValue().ciderMakerId()).isEqualTo(ciderMakerId);
    assertThat(query.getValue().limit()).isEqualTo(50);
  }

  private static ScoreFeedstockBatchProfileTargetService service(
      FeedstockMeasurementQueryRepository repository
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();
    final var geometryProfile = BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    return new ScoreFeedstockBatchProfileTargetService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(thresholds, geometryProfile),
        new ZoneGeometrySugarAcidityTargetResolver(geometryProfile),
        new ScoreProfileTargetDistanceCandidatesService(
            new BuiltInProfileAxisNormalizerSelector(thresholds),
            new BuiltInProfileTargetScoringPurposeNormalizerPolicy(),
            new BuiltInProfileTargetObjectiveFunctionFactory()
        )
    );
  }

  private static FeedstockMeasurement sample(
      String name,
      ChemicalProfile profile
  ) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of(name),
        profile,
        Instant.parse("2026-01-15T10:00:00Z")
    );
  }

  private static ChemicalProfile profile(
      String sg,
      String acidity
  ) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(sg)
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .build();
  }
}
