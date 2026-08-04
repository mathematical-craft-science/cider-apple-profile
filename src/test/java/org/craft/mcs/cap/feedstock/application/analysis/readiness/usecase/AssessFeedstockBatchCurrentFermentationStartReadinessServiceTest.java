package org.craft.mcs.cap.feedstock.application.analysis.readiness.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.FeedstockBatchCurrentFermentationStartReadinessResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.usecase.AssessFeedstockBatchCurrentFermentationStartReadinessService;
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
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockBatchCurrentFermentationStartReadinessServiceTest {

  @Test
  void assessesCurrentMaterialStateProfileForBatch() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = new AssessFeedstockBatchCurrentFermentationStartReadinessService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockCurrentProfileQueryService(currentProfileRepository),
        new FermentationStartReadinessAssessor()
    );
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.of(newUuid());
    final var measurement = measurement(batchId);
    final var currentProfile = currentProfile(measurement);

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, workspaceId)));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.of(currentProfile));

    final var result = service.assess(batchId);

    assertThat(result)
        .returns(batchId, FeedstockBatchCurrentFermentationStartReadinessResult::batchId)
        .returns(workspaceId, FeedstockBatchCurrentFermentationStartReadinessResult::workspaceId)
        .returns(
            currentProfile.currentMaterialStateId(),
            FeedstockBatchCurrentFermentationStartReadinessResult::currentMaterialStateId
        )
        .satisfies(value -> assertThat(value.profileEvidence())
            .extracting(item -> item.evidenceReference().measurementSetId())
            .containsOnly(measurement.measurementId().value()));
    assertThat(result.assessment())
        .returns(FermentationStartReadinessStatus.READY, FermentationStartReadinessAssessment::status);
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
            .ph(PointCharacteristicValue.of(PhValue.of("3.45")))
            .yeastAssimilableNitrogen(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180")))
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
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
