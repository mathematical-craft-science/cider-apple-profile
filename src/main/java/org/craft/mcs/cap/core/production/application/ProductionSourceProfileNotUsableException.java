package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ProductionSourceProfileNotUsableException
    extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final @NonNull ChemicalProfileCapability requiredCapability;

  public ProductionSourceProfileNotUsableException(
      @NonNull FeedstockBatchId batchId,
      @NonNull ChemicalProfileCapability requiredCapability
  ) {
    super("feedstock batch current profile does not satisfy production source capability: "
        + requireNonNull(batchId, "feedstock batch id cannot be null").value());
    this.batchId = batchId;
    this.requiredCapability = requireNonNull(requiredCapability, "required capability cannot be null");
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull ChemicalProfileCapability requiredCapability() {
    return requiredCapability;
  }
}
