package org.craft.mcs.cap.feedstock.application.batch.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockMeasurementChangePolicyService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class FeedstockMeasurementChangePolicyServiceTest {

  @Test
  void allowsMeasurementChangesForActiveBatch() {
    final var batch = activeBatch();
    final var repository = mock(FeedstockBatchRepository.class);
    when(repository.lockForMeasurementChange(batch.batchId())).thenReturn(Optional.of(batch));
    final var service = new FeedstockMeasurementChangePolicyService(repository);

    assertThatCode(() -> service.requireChangesAllowed(batch.batchId())).doesNotThrowAnyException();

    verify(repository).lockForMeasurementChange(batch.batchId());
  }

  @ParameterizedTest
  @EnumSource(value = BatchLifecycle.class, names = {"DEPLETED", "DISCARDED"})
  void rejectsMeasurementChangesForInactiveBatch(BatchLifecycle lifecycle) {
    final var batch = inactiveBatch(lifecycle);
    final var repository = mock(FeedstockBatchRepository.class);
    when(repository.lockForMeasurementChange(batch.batchId())).thenReturn(Optional.of(batch));
    final var service = new FeedstockMeasurementChangePolicyService(repository);

    assertThatThrownBy(() -> service.requireChangesAllowed(batch.batchId()))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch measurements can be changed only when lifecycle is ACTIVE");
  }

  private static FeedstockBatch activeBatch() {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Fresh juice"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")),
        VolumeValue.ofLiters("100")
    );
  }

  private static FeedstockBatch inactiveBatch(BatchLifecycle lifecycle) {
    return FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Consumed juice"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31")),
        null,
        lifecycle,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId(),
        null,
        null
    );
  }
}
