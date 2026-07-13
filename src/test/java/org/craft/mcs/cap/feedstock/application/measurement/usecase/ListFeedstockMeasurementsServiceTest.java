package org.craft.mcs.cap.feedstock.application.measurement.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.usecase.ListFeedstockMeasurementsService;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListFeedstockMeasurementsServiceTest {

  @Test
  void listsSampleSummariesByBatchId() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new ListFeedstockMeasurementsService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class)
    );
    final var batchId = FeedstockBatchId.newId();
    final var batch = mock(FeedstockBatch.class);
    when(batch.ciderMakerId()).thenReturn(CiderMakerId.of(randomUUID()));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    final var summary = new FeedstockMeasurementSummary(
        FeedstockMeasurementId.newId(),
        CiderMakerId.of(randomUUID()),
        batchId,
        FeedstockMeasurementName.of("Late press sample"),
        feedstockMeasurementInstant(),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build(),
        "first lab result"
    );
    when(repository.findSummariesByBatchId(batchId)).thenReturn(List.of(summary));

    final var result = service.listByBatchId(batchId);

    assertThat(result).containsExactly(summary);
  }
}
