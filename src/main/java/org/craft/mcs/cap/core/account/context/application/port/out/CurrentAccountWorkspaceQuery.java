package org.craft.mcs.cap.core.account.context.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.account.context.application.model.WorkspaceMembershipSnapshot;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;

/** Reads active workspace memberships as one joined projection for account context resolution. */
public interface CurrentAccountWorkspaceQuery {

  @NonNull List<@NonNull WorkspaceMembershipSnapshot> findActiveByUserId(
      @NonNull AccountUserId userId
  );
}
