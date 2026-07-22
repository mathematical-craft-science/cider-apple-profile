package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationRecipientMismatchException;
import org.craft.mcs.cap.core.account.invitation.application.port.in.GetIncomingWorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.ListIncomingWorkspaceInvitationsUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.WorkspaceInvitationController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.WorkspaceInvitationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceInvitationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WorkspaceInvitationExceptionHandler.class)
class WorkspaceInvitationWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorkspaceInvitationUseCase workspaceInvitationUseCase;

  @MockitoBean
  private GetIncomingWorkspaceInvitationUseCase getIncomingWorkspaceInvitationUseCase;

  @MockitoBean
  private ListIncomingWorkspaceInvitationsUseCase listIncomingWorkspaceInvitationsUseCase;

  @MockitoBean
  private EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase;

  @Test
  void identifiesInvitationRecipientMismatchWithoutExposingRecipientEmail() throws Exception {
    when(getIncomingWorkspaceInvitationUseCase.get(any()))
        .thenThrow(new WorkspaceInvitationRecipientMismatchException());

    mockMvc.perform(get(
            "/account/workspace-invitations/{invitationId}",
            "019f818a-7008-7000-9ef5-3381945e3c41"
        ))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type")
            .value("urn:problem-type:workspace-invitation-recipient-mismatch"))
        .andExpect(jsonPath("$.title").value("Workspace invitation belongs to another account"))
        .andExpect(jsonPath("$.detail")
            .value("workspace invitation was issued to a different account email"))
        .andExpect(jsonPath("$.invitedEmail").doesNotExist());
  }
}
