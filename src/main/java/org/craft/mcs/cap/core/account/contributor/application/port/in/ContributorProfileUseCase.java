package org.craft.mcs.cap.core.account.contributor.application.port.in;

import org.jspecify.annotations.NonNull;

/** Manages the current account's optional public contributor profile. */
public interface ContributorProfileUseCase {

  @NonNull ContributorProfileView get();

  @NonNull ContributorProfileView configure(@NonNull ConfigureContributorProfileCommand command);

  void delete();
}
