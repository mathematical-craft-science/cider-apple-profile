package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.preferences;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for UserPreferences JDBC entities. */
interface SpringDataUserPreferencesJdbcRepository
    extends ListCrudRepository<UserPreferencesJdbcEntity, @NonNull UUID> {
}
