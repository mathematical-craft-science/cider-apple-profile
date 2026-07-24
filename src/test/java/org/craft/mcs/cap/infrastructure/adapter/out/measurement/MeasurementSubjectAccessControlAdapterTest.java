package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchAccessUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchAccessUseCase;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MeasurementSubjectAccessControlAdapterTest {

  @Test
  void delegatesFeedstockSubjectAccessThroughItsInboundBoundary() {
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var feedstockAccess = mock(FeedstockBatchAccessUseCase.class);
    final var adapter = new MeasurementSubjectAccessControlAdapter(
        feedstockAccess,
        mock(ProductionBatchAccessUseCase.class)
    );
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        batchId.value()
    );
    when(feedstockAccess.requireReadAccess(batchId)).thenReturn(workspaceId);
    when(feedstockAccess.requireWriteAccess(batchId)).thenReturn(workspaceId);

    assertThat(adapter)
        .returns(workspaceId, value -> value.requireReadAccess(subject))
        .returns(workspaceId, value -> value.requireWriteAccess(subject));
    adapter.requireReadAccess(MeasurementSubjectType.FEEDSTOCK_BATCH, workspaceId);

    verify(feedstockAccess).requireWorkspaceReadAccess(workspaceId);
  }

  @Test
  void delegatesProductionSubjectAccessThroughItsInboundBoundary() {
    final var batchId = ProductionBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var productionAccess = mock(ProductionBatchAccessUseCase.class);
    final var adapter = new MeasurementSubjectAccessControlAdapter(
        mock(FeedstockBatchAccessUseCase.class),
        productionAccess
    );
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        batchId.value()
    );
    when(productionAccess.requireReadAccess(batchId)).thenReturn(workspaceId);
    when(productionAccess.requireWriteAccess(batchId)).thenReturn(workspaceId);

    assertThat(adapter)
        .returns(workspaceId, value -> value.requireReadAccess(subject))
        .returns(workspaceId, value -> value.requireWriteAccess(subject));
    adapter.requireReadAccess(MeasurementSubjectType.PRODUCTION_BATCH, workspaceId);

    verify(productionAccess).requireWorkspaceReadAccess(workspaceId);
  }
}
