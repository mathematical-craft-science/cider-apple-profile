package org.craft.mcs.cap.feedstock.application.query;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockBatchCurrentMeasurementQueryServiceTest {

  @Test
  void resolvesCurrentMeasurementThroughLatestMeasurementRepositoryQuery() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = new FeedstockBatchCurrentMeasurementQueryService(repository);
    final var batchId = FeedstockBatchId.newId();
    final var measurement = measurement(batchId);

    when(repository.findLatestByBatchId(batchId)).thenReturn(Optional.of(measurement));

    assertThat(service.requireCurrentMeasurement(batchId)).isSameAs(measurement);
  }

  @Test
  void rejectsBatchWithoutMeasurements() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = new FeedstockBatchCurrentMeasurementQueryService(repository);
    final var batchId = FeedstockBatchId.newId();

    when(repository.findLatestByBatchId(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.requireCurrentMeasurement(batchId))
        .isInstanceOf(FeedstockBatchHasNoMeasurementsException.class);
  }

  private static FeedstockMeasurement measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current measurement"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build(),
        feedstockMeasurementInstant()
    );
  }
}
