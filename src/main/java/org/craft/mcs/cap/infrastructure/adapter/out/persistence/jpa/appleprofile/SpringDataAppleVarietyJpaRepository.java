package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


interface SpringDataAppleVarietyJpaRepository
    extends JpaRepository<AppleVarietyJpaEntity, @NonNull UUID> {

  @NonNull
  @Query("""
      select variety
      from AppleVarietyJpaEntity variety
      where (:nameContains is null or lower(variety.name) like concat('%', :nameContains, '%'))
        and (
          :afterName is null
          or variety.name > :afterName
          or (variety.name = :afterName and variety.varietyId > :afterVarietyId)
        )
      order by variety.name asc, variety.varietyId asc
      """)
  List<@NonNull AppleVarietyJpaEntity> findSugarAcidityPointCandidates(
      @Param("nameContains") @Nullable String nameContains,
      @Param("afterName") @Nullable String afterName,
      @Param("afterVarietyId") @Nullable UUID afterVarietyId,
      @NonNull Pageable pageable
  );
}
