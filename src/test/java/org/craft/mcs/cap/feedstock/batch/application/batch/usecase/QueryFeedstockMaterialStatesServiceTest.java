package org.craft.mcs.cap.feedstock.batch.application.batch.usecase;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockMaterialStateQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase.QueryFeedstockMaterialStatesService;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class QueryFeedstockMaterialStatesServiceTest {

  @Test
  void returnsStatesOwnedByAuthorizedWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    final var batchId = FeedstockBatchId.newId();
    final var stateId = MaterialStateId.newId();
    final var accessControl = mock(FeedstockAccessControl.class);
    final var stateQuery = mock(FeedstockMaterialStateQuery.class);
    final var service = new QueryFeedstockMaterialStatesService(accessControl, stateQuery);
    when(stateQuery.findCurrentStates(workspaceId, Set.of(batchId)))
        .thenReturn(Map.of(batchId, stateId));

    assertThat(service.findCurrentStates(workspaceId, Set.of(batchId)))
        .containsExactlyEntriesOf(Map.of(batchId, stateId));
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void rejectsMissingFeedstockBatch() {
    final var workspaceId = WorkspaceId.newId();
    final var batchId = FeedstockBatchId.newId();
    final var stateQuery = mock(FeedstockMaterialStateQuery.class);
    final var service = new QueryFeedstockMaterialStatesService(
        mock(FeedstockAccessControl.class),
        stateQuery
    );
    when(stateQuery.findCurrentStates(workspaceId, Set.of(batchId))).thenReturn(Map.of());

    assertThatThrownBy(() -> service.findCurrentStates(workspaceId, Set.of(batchId)))
        .isInstanceOf(FeedstockBatchNotFoundException.class);
  }

  @Test
  void authorizesEmptyQueryWithoutCallingPersistence() {
    final var workspaceId = WorkspaceId.newId();
    final var accessControl = mock(FeedstockAccessControl.class);
    final var stateQuery = mock(FeedstockMaterialStateQuery.class);
    final var service = new QueryFeedstockMaterialStatesService(accessControl, stateQuery);

    assertThat(service.findCurrentStates(workspaceId, Set.of())).isEmpty();
    verify(accessControl).requireReadAccess(workspaceId);
    verifyNoInteractions(stateQuery);
  }
}
