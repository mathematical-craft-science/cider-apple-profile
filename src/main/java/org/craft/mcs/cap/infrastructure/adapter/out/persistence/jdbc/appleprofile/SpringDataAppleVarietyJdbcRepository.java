package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity.AppleVarietyJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


interface SpringDataAppleVarietyJdbcRepository
    extends ListCrudRepository<AppleVarietyJdbcEntity, @NonNull UUID> {
}
