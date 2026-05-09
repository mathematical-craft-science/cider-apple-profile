package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockBatchJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;


interface SpringDataFeedstockBatchJpaRepository
    extends JpaRepository<FeedstockBatchJpaEntity, @NonNull UUID> {

  @NonNull
  List<@NonNull FeedstockBatchJpaEntity> findAllByCiderMakerId(@NonNull UUID ciderMakerId);
}
