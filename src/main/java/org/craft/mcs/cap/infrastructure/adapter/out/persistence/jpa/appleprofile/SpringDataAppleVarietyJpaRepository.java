package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaEntity;
import org.jspecify.annotations.NonNull;
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
      where (:filterByName = false or lower(variety.name) like concat('%', :nameContains, '%'))
        and (
          :filterByCursor = false
          or variety.name > :afterName
          or (variety.name = :afterName and variety.varietyId > :afterVarietyId)
        )
      order by variety.name asc, variety.varietyId asc
      """)
  List<@NonNull AppleVarietyJpaEntity> findSugarAcidityPointCandidates(
      @Param("filterByName") boolean filterByName,
      @Param("nameContains") @NonNull String nameContains,
      @Param("filterByCursor") boolean filterByCursor,
      @Param("afterName") @NonNull String afterName,
      @Param("afterVarietyId") @NonNull UUID afterVarietyId,
      @NonNull Pageable pageable
  );
}
