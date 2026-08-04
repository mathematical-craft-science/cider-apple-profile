package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.QueryFeedstockMaterialStatesUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.port.in.QueryProductionMaterialStatesUseCase;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class OwnerContextCurrentMaterialStateProviderTest {

  @Test
  void resolvesEachSubjectThroughItsOwningContext() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstockBatchId = FeedstockBatchId.newId();
    final var productionBatchId = ProductionBatchId.newId();
    final var feedstockStateId = MaterialStateId.newId();
    final var productionStateId = MaterialStateId.newId();
    final var feedstockStates = mock(QueryFeedstockMaterialStatesUseCase.class);
    final var productionStates = mock(QueryProductionMaterialStatesUseCase.class);
    final var provider = new OwnerContextCurrentMaterialStateProvider(feedstockStates, productionStates);
    when(feedstockStates.findCurrentStates(workspaceId, Set.of(feedstockBatchId)))
        .thenReturn(Map.of(feedstockBatchId, feedstockStateId));
    when(productionStates.findCurrentStates(workspaceId, Set.of(productionBatchId)))
        .thenReturn(Map.of(productionBatchId, productionStateId));
    final var feedstockSubject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        feedstockBatchId.value()
    );
    final var productionSubject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        productionBatchId.value()
    );

    assertThat(provider.currentStatesOf(workspaceId, Set.of(feedstockSubject, productionSubject)))
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            feedstockSubject,
            feedstockStateId,
            productionSubject,
            productionStateId
        ));
  }

  @Test
  void rejectsAnOwnerContextThatDoesNotResolveEverySubject() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstockBatchId = FeedstockBatchId.newId();
    final var feedstockStates = mock(QueryFeedstockMaterialStatesUseCase.class);
    final var productionStates = mock(QueryProductionMaterialStatesUseCase.class);
    final var provider = new OwnerContextCurrentMaterialStateProvider(
        feedstockStates,
        productionStates
    );
    when(feedstockStates.findCurrentStates(workspaceId, Set.of(feedstockBatchId))).thenReturn(Map.of());
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        feedstockBatchId.value()
    );

    assertThatThrownBy(() -> provider.currentStatesOf(workspaceId, Set.of(subject)))
        .isInstanceOf(ApplicationValidationException.class);
    verifyNoInteractions(productionStates);
  }
}
