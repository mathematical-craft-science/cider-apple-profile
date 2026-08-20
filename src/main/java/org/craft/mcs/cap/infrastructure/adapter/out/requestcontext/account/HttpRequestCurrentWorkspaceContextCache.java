package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account;

import java.util.Optional;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentWorkspaceContextRequestCache;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static java.util.Objects.requireNonNull;

/** Stores the verified active workspace context only for the lifetime of one HTTP request. */
@Component
@RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
public final class HttpRequestCurrentWorkspaceContextCache
    implements CurrentWorkspaceContextRequestCache {

  private @Nullable ResolvedWorkspaceContext current;

  @Override
  public @NonNull Optional<@NonNull ResolvedWorkspaceContext> current() {
    return Optional.ofNullable(current);
  }

  @Override
  public void remember(@NonNull ResolvedWorkspaceContext context) {
    current = requireNonNull(context, "current workspace context cannot be null");
  }
}
