package org.craft.mcs.cap.production.batch.application.usecase;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMaterialStateQuery;
import org.craft.mcs.cap.core.production.batch.application.usecase.QueryProductionMaterialStatesService;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class QueryProductionMaterialStatesServiceTest {

  @Test
  void returnsStatesOwnedByAuthorizedWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    final var batchId = ProductionBatchId.newId();
    final var stateId = MaterialStateId.newId();
    final var accessControl = mock(ProductionAccessControl.class);
    final var stateQuery = mock(ProductionMaterialStateQuery.class);
    final var service = new QueryProductionMaterialStatesService(accessControl, stateQuery);
    when(stateQuery.findCurrentStates(workspaceId, Set.of(batchId)))
        .thenReturn(Map.of(batchId, stateId));

    assertThat(service.findCurrentStates(workspaceId, Set.of(batchId)))
        .containsExactlyEntriesOf(Map.of(batchId, stateId));
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void rejectsMissingProductionBatch() {
    final var workspaceId = WorkspaceId.newId();
    final var batchId = ProductionBatchId.newId();
    final var stateQuery = mock(ProductionMaterialStateQuery.class);
    final var service = new QueryProductionMaterialStatesService(
        mock(ProductionAccessControl.class),
        stateQuery
    );
    when(stateQuery.findCurrentStates(workspaceId, Set.of(batchId))).thenReturn(Map.of());

    assertThatThrownBy(() -> service.findCurrentStates(workspaceId, Set.of(batchId)))
        .isInstanceOf(ProductionBatchNotFoundException.class);
  }

  @Test
  void authorizesEmptyQueryWithoutCallingPersistence() {
    final var workspaceId = WorkspaceId.newId();
    final var accessControl = mock(ProductionAccessControl.class);
    final var stateQuery = mock(ProductionMaterialStateQuery.class);
    final var service = new QueryProductionMaterialStatesService(accessControl, stateQuery);

    assertThat(service.findCurrentStates(workspaceId, Set.of())).isEmpty();
    verify(accessControl).requireReadAccess(workspaceId);
    verifyNoInteractions(stateQuery);
  }
}
