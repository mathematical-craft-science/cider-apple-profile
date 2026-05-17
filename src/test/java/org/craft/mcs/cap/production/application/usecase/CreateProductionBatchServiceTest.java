package org.craft.mcs.cap.production.application.usecase;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CreateProductionBatchServiceTest {

  @Test
  void createsProductionBatchAndConsumesFeedstockVolumes() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var firstBatch = batch(ciderMakerId, "50");
    final var secondBatch = batch(ciderMakerId, "60");
    final var batchRepository = new InMemoryFeedstockBatchRepository(firstBatch, secondBatch);
    final var productionRepository = new CapturingProductionBatchRepository();
    final var service = service(batchRepository, productionRepository);

    final var result = service.create(command(
        ciderMakerId,
        List.of(
            new ProductionSourceComponent(firstBatch.batchId(), VolumeValue.ofLiters("10")),
            new ProductionSourceComponent(secondBatch.batchId(), VolumeValue.ofLiters("30"))
        ),
        profileSnapshot("1.045", "8.0", "900")
    ));

    assertThat(result.productionBatch().kind()).isEqualTo(ProductionBatchKind.CIDER);
    assertThat(result.productionBatch().volume().liters()).isEqualByComparingTo("40");
    assertThat(result.productionBatch().profile().sugar().asBigDecimal()).isEqualByComparingTo("1.045");
    assertThat(result.productionBatch().profile().acidity().asBigDecimal()).isEqualByComparingTo("8.0");
    assertThat(result.productionBatch().profile().tannin()).isNotNull();
    assertThat(result.input().targetDistance()).isNotNull();
    assertThat(result.input().targetDistance().value()).isEqualByComparingTo("0.125");
    assertThat(batchRepository.findById(firstBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("40");
    assertThat(batchRepository.findById(secondBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("30");
    assertThat(productionRepository.saved).isNotNull();
  }

  @Test
  void rejectsSourceOwnedByAnotherCiderMaker() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var otherBatch = batch(CiderMakerId.of(randomUUID()), "50");
    final var batchRepository = new InMemoryFeedstockBatchRepository(otherBatch);
    final var service = service(
        batchRepository,
        new CapturingProductionBatchRepository()
    );

    assertThatThrownBy(() -> service.create(command(
        ciderMakerId,
        List.of(new ProductionSourceComponent(otherBatch.batchId(), VolumeValue.ofLiters("10"))),
        profileSnapshot("1.060", "5.0", null)
    )))
        .isInstanceOf(ProductionSourceOwnershipMismatchException.class);

    assertThat(batchRepository.findById(otherBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("50");
  }

  @Test
  void rejectsSourceVolumeGreaterThanAvailableVolume() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batch = batch(ciderMakerId, "5");
    final var service = service(
        new InMemoryFeedstockBatchRepository(batch),
        new CapturingProductionBatchRepository()
    );

    assertThatThrownBy(() -> service.create(command(
        ciderMakerId,
        List.of(new ProductionSourceComponent(batch.batchId(), VolumeValue.ofLiters("10"))),
        profileSnapshot("1.060", "5.0", null)
    )))
        .isInstanceOf(ProductionSourceInsufficientAvailableVolumeException.class);
  }

  private static @NonNull CreateProductionBatchService service(
      @NonNull FeedstockBatchRepository batchRepository,
      @NonNull ProductionBatchRepository productionRepository
  ) {
    return new CreateProductionBatchService(
        batchRepository,
        productionRepository
    );
  }

  private static @NonNull CreateProductionBatchCommand command(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull List<@NonNull ProductionSourceComponent> sources,
      @NonNull ProductionProfileSnapshot profile
  ) {
    return new CreateProductionBatchCommand(
        ciderMakerId,
        ProductionBatchName.of("Tank A"),
        ProductionBatchKind.CIDER,
        new ProductionInput(
            ProductionSourceComposition.of(sources),
            profile,
            ProductionTargetDistance.of(new java.math.BigDecimal("0.125"))
        ),
        null
    );
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull String availableVolumeLiters
  ) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of("Harvest batch"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters(availableVolumeLiters)
    );
  }

  private static @NonNull ProductionProfileSnapshot profileSnapshot(
      @NonNull String sg,
      @NonNull String acidity,
      String tannin
  ) {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(sg),
        TitratableAcidityValue.of(acidity),
        tannin == null ? null : TanninityValue.of(tannin)
    );
  }

  private static final class InMemoryFeedstockBatchRepository
      implements FeedstockBatchRepository {

    private final @NonNull HashMap<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batches = new HashMap<>();

    InMemoryFeedstockBatchRepository(@NonNull FeedstockBatch... batches) {
      for (final var batch : batches) {
        this.batches.put(batch.batchId(), batch);
      }
    }

    @Override
    public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
      batches.put(feedstockBatch.batchId(), feedstockBatch);
      return feedstockBatch;
    }

    @Override
    public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
      return Optional.ofNullable(batches.get(batchId));
    }

    @Override
    public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return batches.values().stream()
          .filter(batch -> batch.ciderMakerId().equals(ciderMakerId))
          .toList();
    }

    @Override
    public void delete(@NonNull FeedstockBatch feedstockBatch) {
      batches.remove(feedstockBatch.batchId());
    }
  }

  private static final class CapturingProductionBatchRepository
      implements ProductionBatchRepository {

    private ProductionBatch saved;

    @Override
    public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
      saved = productionBatch;
      return productionBatch;
    }
  }
}
