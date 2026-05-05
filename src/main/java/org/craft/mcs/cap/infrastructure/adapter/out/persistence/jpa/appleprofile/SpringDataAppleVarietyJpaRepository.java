package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;


interface SpringDataAppleVarietyJpaRepository
    extends JpaRepository<AppleVarietyJpaEntity, @NonNull UUID> {
}