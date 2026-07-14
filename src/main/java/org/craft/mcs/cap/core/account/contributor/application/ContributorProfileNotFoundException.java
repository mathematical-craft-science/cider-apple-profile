package org.craft.mcs.cap.core.account.contributor.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;

/** Raised when the current account has not created a contributor profile. */
@SuppressWarnings("java:S110")
public final class ContributorProfileNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ContributorProfileNotFoundException() {
    super("contributor profile not found for current account");
  }
}
