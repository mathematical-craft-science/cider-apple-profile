package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Supplies the workspace explicitly selected for the current stateless request. */
public interface ActiveWorkspaceProvider {

  @NonNull Optional<@NonNull WorkspaceId> selectedWorkspaceId();
}
