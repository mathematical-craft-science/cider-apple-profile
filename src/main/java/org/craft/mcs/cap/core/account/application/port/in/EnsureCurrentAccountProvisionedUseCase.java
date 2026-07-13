package org.craft.mcs.cap.core.account.application.port.in;


/** Ensures that the authenticated external identity has a complete CAP account context. */
public interface EnsureCurrentAccountProvisionedUseCase {

  void ensureProvisioned();
}
