package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateProductionBatchCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull ProductionBatchName name,
    @NonNull ProductionBatchKind kind,
    @NonNull ProductionInput input,
    @Nullable String note
) {

  public CreateProductionBatchCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(kind, "production batch kind cannot be null");
    requireNonNull(input, "production input cannot be null");

    final var sources = input.sourceComposition().sources();
    if (sources.stream().map(ProductionSourceComponent::feedstockBatchId).distinct().count() != sources.size()) {
      throw new IllegalArgumentException("production batch cannot contain duplicate feedstock batch sources");
    }
  }
}
