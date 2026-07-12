package org.craft.mcs.cap.core.production.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/**
 * Outbound port for source-specific validation, volume accounting and trace snapshots.
 *
 * <p>Production confirms that a production batch should be created from accepted sources. The
 * source-owning context decides whether each source exists, belongs to the cider maker, has enough
 * available volume, and how its current material snapshot is built.
 */
public interface ProductionSourceConsumptionPort {

  @NonNull
  List<@NonNull ProductionBatchTraceSource> consumeAndSnapshotSources(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull List<@NonNull ProductionSourceComponent> sources
  );
}
