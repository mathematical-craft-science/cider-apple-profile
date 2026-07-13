package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for optional public contributor profiles. */
public interface ContributorProfileRepository {

  @NonNull ContributorProfile save(@NonNull ContributorProfile profile);

  @NonNull Optional<@NonNull ContributorProfile> findByUserId(@NonNull AccountUserId userId);
}
