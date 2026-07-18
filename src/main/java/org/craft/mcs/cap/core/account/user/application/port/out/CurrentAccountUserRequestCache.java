package org.craft.mcs.cap.core.account.user.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.jspecify.annotations.NonNull;

/**
 * Holds the verified CAP account for the current application request.
 *
 * <p>The port prevents repeated identity lookups without coupling account application services to HTTP scope.
 */
public interface CurrentAccountUserRequestCache {

  @NonNull Optional<@NonNull AccountUser> current();

  void remember(@NonNull AccountUser user);
}
