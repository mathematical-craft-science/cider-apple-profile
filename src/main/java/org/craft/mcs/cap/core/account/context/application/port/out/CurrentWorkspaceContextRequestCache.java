package org.craft.mcs.cap.core.account.context.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.jspecify.annotations.NonNull;

/** Stores a verified active workspace context for the current application request. */
public interface CurrentWorkspaceContextRequestCache {

  @NonNull Optional<@NonNull ResolvedWorkspaceContext> current();

  void remember(@NonNull ResolvedWorkspaceContext context);
}
