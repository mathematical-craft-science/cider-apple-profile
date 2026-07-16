package org.craft.mcs.cap.core.account.contributor.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for optional public contributor profiles. */
public interface ContributorProfileRepository
    extends VersionedAggregateRepository<ContributorProfile, AccountUserId> {

  void delete(@NonNull VersionedAggregate<ContributorProfile> profile);

  default @NonNull Optional<@NonNull ContributorProfile> findByUserId(@NonNull AccountUserId userId) {
    return findById(userId);
  }
}
