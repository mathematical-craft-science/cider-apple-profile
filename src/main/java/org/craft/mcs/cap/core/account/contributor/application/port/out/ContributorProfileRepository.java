package org.craft.mcs.cap.core.account.contributor.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for optional public contributor profiles. */
public interface ContributorProfileRepository {

  @NonNull ContributorProfile save(@NonNull ContributorProfile profile);

  void deleteByUserId(@NonNull AccountUserId userId);

  @NonNull Optional<@NonNull ContributorProfile> findByUserId(@NonNull AccountUserId userId);
}
