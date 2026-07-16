package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.mapper.AccountContextRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper.WorkspaceMembershipRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceListResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Thin REST adapter for the authenticated account and active-workspace context. */
@RestController
@RequiredArgsConstructor
public final class AccountContextController implements AccountContextApi {

  private final @NonNull GetCurrentAccountUseCase getCurrentAccountUseCase;
  private final @NonNull ListAccountWorkspacesUseCase listAccountWorkspacesUseCase;

  @Override
  @GetMapping(value = "/account/me", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountMeResponse> me() {
    return ResponseEntity.ok(AccountContextRestMapper.toResponse(getCurrentAccountUseCase.get()));
  }

  @Override
  @GetMapping(value = "/account/workspaces", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceListResponse> workspaces() {
    return ResponseEntity.ok(new AccountWorkspaceListResponse(
        listAccountWorkspacesUseCase.list().stream()
            .map(WorkspaceMembershipRestMapper::toResponse)
            .toList()
    ));
  }
}
