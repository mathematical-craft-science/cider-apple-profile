package org.craft.mcs.cap.core.production.application.port.out;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Outbound port for recording production-batch traceability facts.
 *
 * <p>The production context owns the fact that a production batch was created from an accepted
 * input. It does not know how traceability is persisted or projected.
 */
public interface ProductionBatchTraceRecorder {

  void recordCreated(
      @NonNull ProductionBatch productionBatch,
      @NonNull List<@NonNull ConsumedProductionSource> sources,
      @NonNull Instant occurredAt,
      @Nullable String note
  );
}
