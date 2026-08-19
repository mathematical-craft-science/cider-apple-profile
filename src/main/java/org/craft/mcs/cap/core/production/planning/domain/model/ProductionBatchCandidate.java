package org.craft.mcs.cap.core.production.planning.domain.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
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
@AggregateRoot
public final class ProductionBatchCandidate {

  @Identity
  private final @NonNull ProductionBatchCandidateId candidateId;
  private final @NonNull WorkspaceId workspaceId;
  private final @NonNull ProductionSourceComposition sourceComposition;
  private final @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
      sourceStateEvidence;
  private final @NonNull ProductionProfileSnapshot profile;
  private @NonNull ProductionBatchCandidateStatus status;
  private @Nullable ProductionBatchId confirmedProductionBatchId;

  @SuppressWarnings("java:S107")
  private ProductionBatchCandidate(
      @NonNull ProductionBatchCandidateId candidateId,
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          sourceStateEvidence,
      @NonNull ProductionProfileSnapshot profile,
      @NonNull ProductionBatchCandidateStatus status,
      @Nullable ProductionBatchId confirmedProductionBatchId
  ) {
    this.candidateId = requireNonNull(candidateId, "production batch candidate id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.sourceComposition = requireNonNull(sourceComposition, "production source composition cannot be null");
    this.sourceStateEvidence = Map.copyOf(
        requireNonNull(sourceStateEvidence, "production source state evidence cannot be null")
    );
    this.profile = requireNonNull(profile, "production candidate profile cannot be null");
    this.status = requireNonNull(status, "production batch candidate status cannot be null");
    this.confirmedProductionBatchId = confirmedProductionBatchId;
    validateSourceStateEvidence(sourceComposition, this.sourceStateEvidence);
    validateConfirmationState(status, confirmedProductionBatchId);
  }

  public static @NonNull ProductionBatchCandidate create(
      @NonNull ProductionBatchCandidateId candidateId,
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          sourceStateEvidence,
      @NonNull ProductionProfileSnapshot profile
  ) {
    return new ProductionBatchCandidate(
        candidateId,
        workspaceId,
        sourceComposition,
        sourceStateEvidence,
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
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          sourceStateEvidence,
      @NonNull ProductionProfileSnapshot profile,
      @NonNull ProductionBatchCandidateStatus status,
      @Nullable ProductionBatchId confirmedProductionBatchId
  ) {
    return new ProductionBatchCandidate(
        candidateId,
        workspaceId,
        sourceComposition,
        sourceStateEvidence,
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
    return stateEvidenceFor(sourceReference).evidenceReference();
  }

  public @NonNull MaterialStateId materialStateFor(
      @NonNull ProductionSourceReference sourceReference
  ) {
    return stateEvidenceFor(sourceReference).materialStateId();
  }

  public @NonNull ProductionCandidateSourceEvidence stateEvidenceFor(
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var stateEvidence = sourceStateEvidence.get(requireNonNull(
        sourceReference,
        "production source reference cannot be null"
    ));
    if (stateEvidence == null) {
      throw new DomainRuleViolationException("production source state evidence is missing");
    }
    return stateEvidence;
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

  public @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
      sourceStateEvidence() {
    return sourceStateEvidence;
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

  private static void validateSourceStateEvidence(
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          sourceStateEvidence
  ) {
    if (!sourceReferences(sourceComposition).equals(sourceStateEvidence.keySet())) {
      throw new DomainRuleViolationException("production source state evidence must match candidate sources");
    }
  }

  private static @NonNull Set<@NonNull ProductionSourceReference> sourceReferences(
      @NonNull ProductionSourceComposition sourceComposition
  ) {
    return sourceComposition.sources().stream()
        .map(ProductionSourceComponent::reference)
        .collect(Collectors.toUnmodifiableSet());
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
