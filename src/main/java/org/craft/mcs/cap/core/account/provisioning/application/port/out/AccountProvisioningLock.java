package org.craft.mcs.cap.core.account.provisioning.application.port.out;

import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;

/** Serializes first-login provisioning for one external identity within the current transaction. */
public interface AccountProvisioningLock {

  void acquire(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  );
}
