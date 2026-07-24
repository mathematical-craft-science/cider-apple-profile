package org.craft.mcs.cap.infrastructure.adapter.out.production.source;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.production.source.FeedstockProductionSourceFailureTranslator.translate;


/** Anti-corruption adapter for inspecting Feedstock evidence used by production candidates. */
@Component
@RequiredArgsConstructor
public final class FeedstockProductionSourceInspector implements ProductionSourceInspector {

  private final @NonNull InspectFeedstockBatchMaterialUseCase inspectFeedstockBatchMaterialUseCase;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;

  @Override
  public @NonNull AvailableProductionSource inspect(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(source, "production source component cannot be null");
    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> inspectFeedstockBatch(workspaceId, source);
    };
  }

  private @NonNull AvailableProductionSource inspectFeedstockBatch(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  ) {
    final var snapshot = translate(source.reference(), () -> inspectFeedstockBatchMaterialUseCase.inspect(
        new InspectFeedstockBatchMaterialCommand(
            workspaceId,
            FeedstockBatchId.of(source.reference().id()),
            source.volume()
        )
    ));
    return new AvailableProductionSource(
        source.reference(),
        ProductionSourceDisplayName.of(snapshot.batchName().value()),
        snapshot.requestedVolume(),
        snapshot.availableVolume(),
        axisExtractor.extract(snapshot.chemicalProfile()),
        snapshot.profileConfidence(),
        ProductionSourceEvidenceReference.of(snapshot.evidenceReference().externalValue())
    );
  }
}
