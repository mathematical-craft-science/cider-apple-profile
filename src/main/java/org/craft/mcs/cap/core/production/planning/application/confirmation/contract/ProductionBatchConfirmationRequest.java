package org.craft.mcs.cap.core.production.planning.application.confirmation.contract;

import java.util.Map;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Planning-owned request for materializing an accepted candidate as a production batch. */
public record ProductionBatchConfirmationRequest(
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @NonNull VesselId vesselId,
    @NonNull ProductionSourceComposition sourceComposition,
    @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
        sourceEvidence,
    @NonNull ProductionProfileSnapshot profile,
    @Nullable String note
) {

  public ProductionBatchConfirmationRequest {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(vesselId, "production vessel id cannot be null");
    requireNonNull(sourceComposition, "production source composition cannot be null");
    sourceEvidence = Map.copyOf(requireNonNull(sourceEvidence, "production source evidence cannot be null"));
    requireNonNull(profile, "production profile cannot be null");

    final var sourceReferences = sourceComposition.sources().stream()
        .map(ProductionSourceComponent::reference)
        .collect(Collectors.toUnmodifiableSet());
    if (!sourceReferences.equals(sourceEvidence.keySet())) {
      throw new ApplicationValidationException("production source evidence must match confirmation sources");
    }
  }
}
