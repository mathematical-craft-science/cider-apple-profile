package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "production_batch_sources")
public class ProductionBatchSourceJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  Long id;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "production_batch_id", nullable = false, updatable = false)
  ProductionBatchJpaEntity productionBatch;

  @NonNull
  @Column(name = "feedstock_batch_id", nullable = false, updatable = false)
  UUID feedstockBatchId;

  @NonNull
  @Column(name = "volume_liters", nullable = false, precision = 15, scale = 3)
  BigDecimal volumeLiters;

  @SuppressWarnings("NullAway.Init")
  protected ProductionBatchSourceJpaEntity() {
  }

  ProductionBatchSourceJpaEntity(
      @NonNull UUID feedstockBatchId,
      @NonNull BigDecimal volumeLiters
  ) {
    this.feedstockBatchId = requireNonNull(feedstockBatchId, "feedstock batch id cannot be null");
    this.volumeLiters = requireNonNull(volumeLiters, "production source volume cannot be null");
  }
}
