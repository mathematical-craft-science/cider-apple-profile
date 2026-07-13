package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.ContributorProfileJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for ContributorProfile JDBC entities. */
interface SpringDataContributorProfileJdbcRepository
    extends ListCrudRepository<ContributorProfileJdbcEntity, @NonNull UUID> {
}
