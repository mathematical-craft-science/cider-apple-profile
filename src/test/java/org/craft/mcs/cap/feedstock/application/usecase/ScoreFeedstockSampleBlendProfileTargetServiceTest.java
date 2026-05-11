package org.craft.mcs.cap.feedstock.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase.BuiltInBlendProfileScoringPurposeNormalizerPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase.ScoreBlendProfileDistanceCandidatesService;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BuiltInBlendProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockSampleBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockSampleBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockSampleSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.usecase.ScoreFeedstockSampleBlendProfileTargetService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
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


class ScoreFeedstockSampleBlendProfileTargetServiceTest {

  @Test
  void scoresLatestSampleCandidatesPerBatchAndSortsByDistance() {
    final var repository = mock(FeedstockSampleQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var near = sample("Near", profile("1.070", "6.0"));
    final var far = sample("Far", profile("1.040", "9.0"));

    when(repository.findLatestSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(far, near));

    final var result = service.score(new ScoreFeedstockSampleBlendProfileTargetCommand(
        ciderMakerId,
        new FeedstockSamplePointFilters(null, null, null, null),
        BlendProfileScoringPurpose.BLENDING_PLANNING,
        new PredefinedFeedstockSampleBlendProfileTargetSelection(SugarAcidityZone.PERFECT_APPLES),
        null,
        BlendProfileObjectiveWeights.balanced(),
        50
    ));

    assertThat(result.scores())
        .extracting(FeedstockSampleBlendProfileTargetScore::sampleId)
        .containsExactly(near.sampleId(), far.sampleId());
    assertThat(result.scores().getFirst().distance().value()).isEqualByComparingTo("0");

    final var query = ArgumentCaptor.forClass(LatestFeedstockSampleSugarAcidityPointCandidateQuery.class);
    verify(repository).findLatestSugarAcidityPointCandidates(query.capture());
    verify(repository, never()).findSugarAcidityPointCandidates(any());
    assertThat(query.getValue().ciderMakerId()).isEqualTo(ciderMakerId);
    assertThat(query.getValue().limit()).isEqualTo(50);
  }

  private static ScoreFeedstockSampleBlendProfileTargetService service(
      FeedstockSampleQueryRepository repository
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();
    final var geometryProfile = BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    return new ScoreFeedstockSampleBlendProfileTargetService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(thresholds, geometryProfile),
        new ZoneGeometrySugarAcidityTargetResolver(geometryProfile),
        new ScoreBlendProfileDistanceCandidatesService(
            new BuiltInBlendProfileAxisNormalizerSelector(thresholds),
            new BuiltInBlendProfileScoringPurposeNormalizerPolicy()
        )
    );
  }

  private static FeedstockSample sample(
      String name,
      ChemicalProfile profile
  ) {
    return FeedstockSample.create(
        FeedstockSampleId.newId(),
        FeedstockBatchId.newId(),
        FeedstockSampleName.of(name),
        FeedstockType.APPLE_JUICE,
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
