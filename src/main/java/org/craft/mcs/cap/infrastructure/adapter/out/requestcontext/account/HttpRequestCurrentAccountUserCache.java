package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account;

import java.util.Optional;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static java.util.Objects.requireNonNull;

/** Stores the verified CAP account only for the lifetime of one HTTP request. */
@Component
@RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
public final class HttpRequestCurrentAccountUserCache implements CurrentAccountUserRequestCache {

  private @Nullable AccountUser current;

  @Override
  public @NonNull Optional<@NonNull AccountUser> current() {
    return Optional.ofNullable(current);
  }

  @Override
  public void remember(@NonNull AccountUser user) {
    current = requireNonNull(user, "current account user cannot be null");
  }
}
