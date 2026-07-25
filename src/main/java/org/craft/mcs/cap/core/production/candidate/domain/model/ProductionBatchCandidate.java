package org.craft.mcs.cap.core.production.candidate.domain.model;

import java.util.Map;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Saved, authoritative production scenario that may be confirmed exactly once.
 *
 * <p>The aggregate preserves selected source volumes, their measurement evidence and the calculated
 * profile. Target assessment remains application analysis metadata and does not enter this aggregate.
 */
// TODO: разобраться в сценариях использования. Сейча вроде не реалмзовано на фронте
@AggregateRoot
public final class ProductionBatchCandidate {

  @Identity
  private final @NonNull ProductionBatchCandidateId candidateId;
  private final @NonNull WorkspaceId workspaceId;
  private final @NonNull ProductionSourceComposition sourceComposition;
  private final @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference>
      sourceEvidence;
  private final @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId> sourceMaterialStates;
  private final @NonNull ProductionProfileSnapshot profile;
  private @NonNull ProductionBatchCandidateStatus status;
  private @Nullable ProductionBatchId confirmedProductionBatchId;

  @SuppressWarnings("java:S107")
  private ProductionBatchCandidate(
      @NonNull ProductionBatchCandidateId candidateId,
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference> sourceEvidence,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId> sourceMaterialStates,
      @NonNull ProductionProfileSnapshot profile,
      @NonNull ProductionBatchCandidateStatus status,
      @Nullable ProductionBatchId confirmedProductionBatchId
  ) {
    this.candidateId = requireNonNull(candidateId, "production batch candidate id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.sourceComposition = requireNonNull(sourceComposition, "production source composition cannot be null");
    this.sourceEvidence = Map.copyOf(requireNonNull(sourceEvidence, "production source evidence cannot be null"));
    this.sourceMaterialStates = Map.copyOf(
        requireNonNull(sourceMaterialStates, "production source material states cannot be null")
    );
    this.profile = requireNonNull(profile, "production candidate profile cannot be null");
    this.status = requireNonNull(status, "production batch candidate status cannot be null");
    this.confirmedProductionBatchId = confirmedProductionBatchId;
    validateSourceEvidence(sourceComposition, this.sourceEvidence);
    validateSourceMaterialStates(sourceComposition, this.sourceMaterialStates);
    validateConfirmationState(status, confirmedProductionBatchId);
  }

  public static @NonNull ProductionBatchCandidate create(
      @NonNull ProductionBatchCandidateId candidateId,
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference> sourceEvidence,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId> sourceMaterialStates,
      @NonNull ProductionProfileSnapshot profile
  ) {
    return new ProductionBatchCandidate(
        candidateId,
        workspaceId,
        sourceComposition,
        sourceEvidence,
        sourceMaterialStates,
        profile,
        ProductionBatchCandidateStatus.AVAILABLE,
        null
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull ProductionBatchCandidate restore(
      @NonNull ProductionBatchCandidateId candidateId,
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference> sourceEvidence,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId> sourceMaterialStates,
      @NonNull ProductionProfileSnapshot profile,
      @NonNull ProductionBatchCandidateStatus status,
      @Nullable ProductionBatchId confirmedProductionBatchId
  ) {
    return new ProductionBatchCandidate(
        candidateId,
        workspaceId,
        sourceComposition,
        sourceEvidence,
        sourceMaterialStates,
        profile,
        status,
        confirmedProductionBatchId
    );
  }

  /** Marks this candidate as consumed by the specified production batch. */
  public void confirm(@NonNull ProductionBatchId productionBatchId) {
    requireNonNull(productionBatchId, "confirmed production batch id cannot be null");
    requireAvailable();
    status = ProductionBatchCandidateStatus.CONFIRMED;
    confirmedProductionBatchId = productionBatchId;
  }

  /** Ensures this candidate can still be used for production confirmation. */
  public void requireAvailable() {
    if (status == ProductionBatchCandidateStatus.CONFIRMED) {
      throw new DomainConflictException("production batch candidate has already been confirmed");
    }
  }

  public @NonNull ProductionSourceEvidenceReference evidenceFor(
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var evidence = sourceEvidence.get(requireNonNull(
        sourceReference,
        "production source reference cannot be null"
    ));
    if (evidence == null) {
      throw new DomainRuleViolationException("production source evidence is missing");
    }
    return evidence;
  }

  public @NonNull MaterialStateId materialStateFor(
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var materialStateId = sourceMaterialStates.get(requireNonNull(
        sourceReference,
        "production source reference cannot be null"
    ));
    if (materialStateId == null) {
      throw new DomainRuleViolationException("production source material state is missing");
    }
    return materialStateId;
  }

  public @NonNull ProductionBatchCandidateId candidateId() {
    return candidateId;
  }

  public @NonNull WorkspaceId workspaceId() {
    return workspaceId;
  }

  public @NonNull ProductionSourceComposition sourceComposition() {
    return sourceComposition;
  }

  public @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference>
      sourceEvidence() {
    return sourceEvidence;
  }

  public @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId>
      sourceMaterialStates() {
    return sourceMaterialStates;
  }

  public @NonNull ProductionProfileSnapshot profile() {
    return profile;
  }

  public @NonNull ProductionBatchCandidateStatus status() {
    return status;
  }

  public @Nullable ProductionBatchId confirmedProductionBatchId() {
    return confirmedProductionBatchId;
  }

  private static void validateSourceEvidence(
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference> sourceEvidence
  ) {
    final var references = sourceComposition.sources().stream()
        .map(ProductionSourceComponent::reference)
        .collect(Collectors.toUnmodifiableSet());
    if (references.size() != sourceComposition.sources().size()) {
      throw new DomainRuleViolationException("production batch candidate cannot contain duplicate sources");
    }
    if (!references.equals(sourceEvidence.keySet())) {
      throw new DomainRuleViolationException("production source evidence must match candidate sources");
    }
  }

  private static void validateSourceMaterialStates(
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull MaterialStateId> sourceMaterialStates
  ) {
    final var references = sourceComposition.sources().stream()
        .map(ProductionSourceComponent::reference)
        .collect(Collectors.toUnmodifiableSet());
    if (!references.equals(sourceMaterialStates.keySet())) {
      throw new DomainRuleViolationException("production source material states must match candidate sources");
    }
  }

  private static void validateConfirmationState(
      @NonNull ProductionBatchCandidateStatus status,
      @Nullable ProductionBatchId confirmedProductionBatchId
  ) {
    if (status == ProductionBatchCandidateStatus.AVAILABLE && confirmedProductionBatchId != null) {
      throw new DomainRuleViolationException("available production candidate cannot reference a production batch");
    }
    if (status == ProductionBatchCandidateStatus.CONFIRMED && confirmedProductionBatchId == null) {
      throw new DomainRuleViolationException("confirmed production candidate must reference a production batch");
    }
  }
}
