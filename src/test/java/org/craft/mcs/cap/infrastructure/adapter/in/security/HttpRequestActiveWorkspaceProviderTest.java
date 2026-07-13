package org.craft.mcs.cap.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.infrastructure.adapter.in.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class HttpRequestActiveWorkspaceProviderTest {

  @Test
  void returnsSelectedWorkspaceFromRequestHeader() {
    final var request = mock(HttpServletRequest.class);
    final var workspaceId = UUID.randomUUID();
    when(request.getHeader(ACTIVE_WORKSPACE_ID)).thenReturn(workspaceId.toString());

    assertThat(new HttpRequestActiveWorkspaceProvider(request).selectedWorkspaceId())
        .contains(WorkspaceId.of(workspaceId));
  }

  @Test
  void returnsEmptySelectionForBlankRequestHeader() {
    final var request = mock(HttpServletRequest.class);
    when(request.getHeader(ACTIVE_WORKSPACE_ID)).thenReturn("  ");

    assertThat(new HttpRequestActiveWorkspaceProvider(request).selectedWorkspaceId()).isEmpty();
  }

  @Test
  void rejectsMalformedWorkspaceHeader() {
    final var request = mock(HttpServletRequest.class);
    when(request.getHeader(ACTIVE_WORKSPACE_ID)).thenReturn("not-a-uuid");
    final var provider = new HttpRequestActiveWorkspaceProvider(request);

    assertThatThrownBy(provider::selectedWorkspaceId)
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("active workspace identifier must be a valid UUID");
  }
}
