package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.port.in.ProductionMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class MeasurementSubjectChangePolicyAdapterTest {

  @Test
  void delegatesFeedstockEligibilityToItsOwningContext() {
    final var batchId = FeedstockBatchId.newId();
    final var feedstockPolicy = mock(FeedstockMeasurementChangePolicyUseCase.class);
    final var adapter = new MeasurementSubjectChangePolicyAdapter(
        feedstockPolicy,
        mock(ProductionMeasurementChangePolicyUseCase.class)
    );

    adapter.requireChangesAllowed(new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        batchId.value()
    ));

    verify(feedstockPolicy).requireChangesAllowed(batchId);
  }

  @Test
  void delegatesProductionEligibilityToItsOwningContext() {
    final var batchId = ProductionBatchId.newId();
    final var productionPolicy = mock(ProductionMeasurementChangePolicyUseCase.class);
    final var adapter = new MeasurementSubjectChangePolicyAdapter(
        mock(FeedstockMeasurementChangePolicyUseCase.class),
        productionPolicy
    );

    adapter.requireChangesAllowed(new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        batchId.value()
    ));

    verify(productionPolicy).requireChangesAllowed(batchId);
  }
}
