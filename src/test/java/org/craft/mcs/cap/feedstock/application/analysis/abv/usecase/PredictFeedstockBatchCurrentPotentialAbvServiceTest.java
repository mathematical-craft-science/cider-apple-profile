package org.craft.mcs.cap.feedstock.application.analysis.abv.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.FeedstockPotentialAbvUnavailableException;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.usecase.PredictFeedstockBatchCurrentPotentialAbvService;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PredictFeedstockBatchCurrentPotentialAbvServiceTest {

  @Test
  void preservesCurrentMaterialStateIdentityInPrediction() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.of(newUuid());
    final var measurement = measurement(batchId);
    final var currentProfile = currentProfile(measurement);

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, workspaceId)));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.of(currentProfile));

    final var prediction = service.predict(batchId);

    assertThat(prediction)
        .returns(batchId, FeedstockBatchCurrentPotentialAbvPrediction::batchId)
        .returns(workspaceId, FeedstockBatchCurrentPotentialAbvPrediction::workspaceId)
        .returns(
            currentProfile.currentMaterialStateId(),
            FeedstockBatchCurrentPotentialAbvPrediction::currentMaterialStateId
        )
        .returns(
            currentProfile.evidenceFor(Set.of(SPECIFIC_GRAVITY)).getFirst(),
            FeedstockBatchCurrentPotentialAbvPrediction::specificGravityEvidence
        )
        .returns(currentProfile.specificGravity(), FeedstockBatchCurrentPotentialAbvPrediction::originalGravity);
    assertThat(prediction.prediction().potentialAbv().percent())
        .isEqualByComparingTo("6.825");
  }

  @Test
  void rejectsCurrentProfileWithoutSpecificGravity() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.of(newUuid());
    final var measurement = FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Acidity only"),
        ProfileCharacteristicsFixture.builder()
            .titratableAcidity(
                PointCharacteristicValue.of(TitratableAcidityValue.of("6.0"))
            )
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, workspaceId)));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.of(currentProfile(measurement)));

    assertThatThrownBy(() -> service.predict(batchId))
        .isInstanceOf(FeedstockPotentialAbvUnavailableException.class)
        .hasMessageContaining(batchId.value().toString());
  }

  private static @NonNull PredictFeedstockBatchCurrentPotentialAbvService service(
      @NonNull FeedstockBatchRepository batchRepository,
      @NonNull FeedstockCurrentProfileQueryRepository currentProfileRepository
  ) {
    return new PredictFeedstockBatchCurrentPotentialAbvService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockCurrentProfileQueryService(currentProfileRepository),
        new PotentialAbvPredictor(
            FixedFinalGravityPredictionStrategy.dryCiderDefault(),
            new StandardGravityDropAbvPredictionStrategy()
        )
    );
  }

  private static @NonNull FeedstockMeasurementFixture measurement(
      @NonNull FeedstockBatchId batchId
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current lab result"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.052")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        BatchName.of("Test batch"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
