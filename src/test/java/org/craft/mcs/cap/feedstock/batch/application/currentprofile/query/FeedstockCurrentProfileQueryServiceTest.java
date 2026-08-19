package org.craft.mcs.cap.feedstock.batch.application.currentprofile.query;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockCurrentProfileQueryServiceTest {

  @Test
  void resolvesCurrentProfileThroughStateAwareRepositoryQuery() {
    final var repository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = new FeedstockCurrentProfileQueryService(repository);
    final var batchId = FeedstockBatchId.newId();
    final var measurement = measurement(batchId);

    final var profileView = currentProfile(measurement);
    when(repository.findByBatchId(batchId)).thenReturn(Optional.of(profileView));

    assertThat(service.requireCurrentProfile(batchId)).isSameAs(profileView);
  }

  @Test
  void rejectsBatchWithoutMeasurements() {
    final var repository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = new FeedstockCurrentProfileQueryService(repository);
    final var batchId = FeedstockBatchId.newId();

    when(repository.findByBatchId(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.requireCurrentProfile(batchId))
        .isInstanceOf(FeedstockBatchHasNoMeasurementsException.class);
  }

  private static FeedstockMeasurementFixture measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current measurement"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.050")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }
}
