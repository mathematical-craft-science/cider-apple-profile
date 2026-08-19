package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.shared.application.query.MalformedCursorException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.testing.TestCursorTokenCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockSugarAcidityCursorAdapterTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of(
      UUID.fromString("019bfcc0-4830-7000-8000-000000000002")
  );
  private static final FeedstockCurrentProfileFilters FILTERS =
      new FeedstockCurrentProfileFilters(null, null);
  private static final FeedstockCurrentProfilePagePosition POSITION =
      new FeedstockCurrentProfilePagePosition(
          Instant.parse("2026-07-20T10:00:00Z"),
          UUID.fromString("019bfcc0-4830-7000-8000-000000000003")
      );

  private final TestCursorTokenCodec cursorTokenCodec = new TestCursorTokenCodec();
  private final FeedstockSugarAcidityCursorAdapter adapter =
      new FeedstockSugarAcidityCursorAdapter(cursorTokenCodec);

  @Test
  void roundTripsTypedCurrentPositionThroughOpaqueToken() {
    final var token = adapter.encodeCurrent(POSITION, WORKSPACE_ID, FILTERS, null);

    assertThat(adapter.decodeCurrent(token, WORKSPACE_ID, FILTERS, null))
        .isEqualTo(POSITION);
  }

  @Test
  void rejectsTokenForAnotherWorkspace() {
    final var token = adapter.encodeCurrent(POSITION, WORKSPACE_ID, FILTERS, null);
    final var anotherWorkspaceId = WorkspaceId.of(
        UUID.fromString("019bfcc0-4830-7000-8000-000000000004")
    );

    assertThatThrownBy(() -> adapter.decodeCurrent(token, anotherWorkspaceId, FILTERS, null))
        .isInstanceOf(MalformedCursorException.class);
  }

  @Test
  void rejectsTokenWhenGraphCriteriaChange() {
    final var graphCriteria = new JolicoeurSugarAcidityGraphCriteria(null, true);
    final var token = adapter.encodeCurrent(POSITION, WORKSPACE_ID, FILTERS, graphCriteria);

    assertThatThrownBy(() -> adapter.decodeCurrent(
        token,
        WORKSPACE_ID,
        FILTERS,
        new JolicoeurSugarAcidityGraphCriteria(null, false)
    )).isInstanceOf(MalformedCursorException.class);
  }
}
