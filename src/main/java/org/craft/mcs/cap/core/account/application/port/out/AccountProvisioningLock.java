package org.craft.mcs.cap.core.account.application.port.out;

import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;


/** Serializes first-login provisioning for one external identity within the current transaction. */
public interface AccountProvisioningLock {

  void acquire(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  );
}
