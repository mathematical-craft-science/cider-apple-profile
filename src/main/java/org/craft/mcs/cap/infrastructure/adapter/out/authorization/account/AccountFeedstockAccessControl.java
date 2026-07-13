package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_WRITE;


/** Translates feedstock-owned access operations into CAP account permissions. */
@Component
@RequiredArgsConstructor
public final class AccountFeedstockAccessControl implements FeedstockAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer workspacePermissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull CiderMakerId ciderMakerId) {
    workspacePermissionAuthorizer.requirePermission(ciderMakerId, FEEDSTOCK_READ);
  }

  @Override
  public void requireWriteAccess(@NonNull CiderMakerId ciderMakerId) {
    workspacePermissionAuthorizer.requirePermission(ciderMakerId, FEEDSTOCK_WRITE);
  }
}
