package org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source.FeedstockProductionSourceFailureTranslator.translate;


/**
 * Anti-corruption adapter that exposes feedstock material through the production source boundary.
 *
 * <p>The adapter translates the public Feedstock material-consumption result and application
 * failures into production-owned source concepts. Feedstock ownership, volume and current-profile
 * rules remain behind the Feedstock inbound boundary; no aggregate crosses contexts.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockProductionSourceInventory implements ProductionSourceInventory {

  private final @NonNull ConsumeFeedstockBatchMaterialUseCase consumeFeedstockBatchMaterialUseCase;

  @Override
  public @NonNull ConsumedProductionSource consume(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(source, "production source component cannot be null");

    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> consumeFeedstockBatch(workspaceId, source);
    };
  }

  private @NonNull ConsumedProductionSource consumeFeedstockBatch(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  ) {
    final var reference = source.reference();
    final var batchId = FeedstockBatchId.of(reference.id());
    final var consumed = consumeMaterial(workspaceId, source, batchId);
    return new ConsumedProductionSource(
        reference,
        ProductionSourceDisplayName.of(consumed.batchName().value()),
        consumed.consumption(),
        toProductionSourceCharacteristics(consumed.characteristics()),
        consumed.profileConfidence(),
        ProductionSourceEvidenceReference.of(consumed.evidenceReference().externalValue())
    );
  }

  private static @NonNull ProductionSourceCharacteristics toProductionSourceCharacteristics(
      @NonNull FeedstockMaterialCharacteristics characteristics
  ) {
    return new ProductionSourceCharacteristics(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin(),
        characteristics.yeastAssimilableNitrogen()
    );
  }

  private @NonNull ConsumedFeedstockBatchMaterial consumeMaterial(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source,
      @NonNull FeedstockBatchId batchId
  ) {
    return translate(source.reference(), () -> consumeFeedstockBatchMaterialUseCase.consume(
        new ConsumeFeedstockBatchMaterialCommand(
            workspaceId,
            batchId,
            source.volume()
        )
    ));
  }
}
