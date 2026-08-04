package org.craft.mcs.cap.production.candidate.domain.model;

import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class ProductionBatchCandidateTest {

  private static final ProductionSourceReference SOURCE_REFERENCE =
      ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
  private static final ProductionSourceComponent SOURCE =
      new ProductionSourceComponent(SOURCE_REFERENCE, VolumeValue.ofLiters("80"));
  private static final ProductionSourceComposition COMPOSITION =
      ProductionSourceComposition.of(List.of(SOURCE));
  private static final ProductionSourceEvidenceReference EVIDENCE =
      ProductionSourceEvidenceReference.of("measurement-set-7");
  private static final MaterialStateId MATERIAL_STATE_ID = MaterialStateId.newId();
  private static final ProductionCandidateSourceEvidence STATE_EVIDENCE =
      new ProductionCandidateSourceEvidence(EVIDENCE, MATERIAL_STATE_ID);
  private static final ProductionProfileSnapshot PROFILE = new ProductionProfileSnapshot(
      SpecificGravityValue.of("1.052"),
      TitratableAcidityValue.of("6.2"),
      null,
      null
  );

  @Test
  void createsAvailableCandidateWithAuthoritativeSourceFacts() {
    final var candidateId = ProductionBatchCandidateId.of(newUuid());
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateEvidence = Map.of(SOURCE_REFERENCE, STATE_EVIDENCE);

    final var candidate = ProductionBatchCandidate.create(
        candidateId,
        workspaceId,
        COMPOSITION,
        sourceStateEvidence,
        PROFILE
    );

    assertThat(candidate)
        .returns(candidateId, ProductionBatchCandidate::candidateId)
        .returns(workspaceId, ProductionBatchCandidate::workspaceId)
        .returns(COMPOSITION, ProductionBatchCandidate::sourceComposition)
        .returns(sourceStateEvidence, ProductionBatchCandidate::sourceStateEvidence)
        .returns(PROFILE, ProductionBatchCandidate::profile)
        .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidate::status)
        .returns(null, ProductionBatchCandidate::confirmedProductionBatchId)
        .returns(EVIDENCE, value -> value.evidenceFor(SOURCE_REFERENCE))
        .returns(MATERIAL_STATE_ID, value -> value.materialStateFor(SOURCE_REFERENCE));
  }

  @Test
  void rejectsCandidateWhoseStateEvidenceDoesNotMatchItsSources() {
    final var candidateId = ProductionBatchCandidateId.newId();
    final var workspaceId = WorkspaceId.newId();
    final Map<ProductionSourceReference, ProductionCandidateSourceEvidence> missingStateEvidence = Map.of();

    assertThatThrownBy(() -> ProductionBatchCandidate.create(
        candidateId,
        workspaceId,
        COMPOSITION,
        missingStateEvidence,
        PROFILE
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source state evidence must match candidate sources");
  }

  @Test
  void validatesRestoredConfirmationState() {
    final var candidateId = ProductionBatchCandidateId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateEvidence = Map.of(SOURCE_REFERENCE, STATE_EVIDENCE);
    final var productionBatchId = ProductionBatchId.newId();

    assertThatThrownBy(() -> ProductionBatchCandidate.restore(
        candidateId,
        workspaceId,
        COMPOSITION,
        sourceStateEvidence,
        PROFILE,
        ProductionBatchCandidateStatus.AVAILABLE,
        productionBatchId
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("available production candidate cannot reference a production batch");
    assertThatThrownBy(() -> ProductionBatchCandidate.restore(
        candidateId,
        workspaceId,
        COMPOSITION,
        sourceStateEvidence,
        PROFILE,
        ProductionBatchCandidateStatus.CONFIRMED,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("confirmed production candidate must reference a production batch");
  }

  @Test
  void restoresAndConfirmsCandidateOnlyOnce() {
    final var candidateId = ProductionBatchCandidateId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var sourceStateEvidence = Map.of(SOURCE_REFERENCE, STATE_EVIDENCE);
    final var productionBatchId = ProductionBatchId.newId();
    final var restored = ProductionBatchCandidate.restore(
        candidateId,
        workspaceId,
        COMPOSITION,
        sourceStateEvidence,
        PROFILE,
        ProductionBatchCandidateStatus.AVAILABLE,
        null
    );

    restored.confirm(productionBatchId);

    assertThat(restored)
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status)
        .returns(productionBatchId, ProductionBatchCandidate::confirmedProductionBatchId);
    assertThatThrownBy(() -> restored.confirm(ProductionBatchId.newId()))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("production batch candidate has already been confirmed");
  }

  @Test
  void rejectsLookupOfFactsForAnUnrelatedSource() {
    final var candidate = candidate();
    final var unrelatedSource = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());

    assertThatThrownBy(() -> candidate.evidenceFor(unrelatedSource))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source state evidence is missing");
    assertThatThrownBy(() -> candidate.materialStateFor(unrelatedSource))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source state evidence is missing");
  }

  private static ProductionBatchCandidate candidate() {
    return ProductionBatchCandidate.create(
        ProductionBatchCandidateId.newId(),
        WorkspaceId.newId(),
        COMPOSITION,
        Map.of(SOURCE_REFERENCE, STATE_EVIDENCE),
        PROFILE
    );
  }
}
