package org.craft.mcs.cap.feedstock.application.analysis.readiness.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.usecase.*;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.view;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockBatchCurrentFermentationStartReadinessServiceTest {

  @Test
  void assessesLatestMeasurementForBatch() {
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var measurementQueryRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = new AssessFeedstockBatchCurrentFermentationStartReadinessService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockBatchCurrentMeasurementQueryService(measurementQueryRepository),
        new FermentationStartReadinessAssessor()
    );
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.of(newUuid());
    final var measurement = measurement(batchId);

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, workspaceId)));
    when(measurementQueryRepository.findLatestByBatchId(batchId)).thenReturn(Optional.of(view(measurement)));

    final var result = service.assess(batchId);

    assertThat(result.batchId()).isEqualTo(batchId);
    assertThat(result.workspaceId()).isEqualTo(workspaceId);
    assertThat(result.currentMeasurementId()).isEqualTo(measurement.measurementId());
    assertThat(result.assessment().status()).isEqualTo(FermentationStartReadinessStatus.READY);
  }

  private static FeedstockMeasurement measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current lab result"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .ph(ExactMeasurement.of(PhValue.of("3.45")))
            .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("180")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static FeedstockBatch batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId
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
