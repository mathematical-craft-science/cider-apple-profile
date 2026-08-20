package org.craft.mcs.cap.production.planning.application.confirmation.contract;

import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.planning.application.confirmation.contract.ProductionBatchConfirmationRequest;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionBatchConfirmationRequestTest {

  @Test
  void acceptsEvidenceForEveryConfirmationSource() {
    final var source = source();
    final var evidence = evidence();

    assertThat(request(Map.of(source, evidence), source).sourceEvidence())
        .containsExactlyEntriesOf(Map.of(source, evidence));
  }

  @Test
  void rejectsEvidenceThatDoesNotMatchConfirmationSources() {
    assertThatThrownBy(() -> request(Map.of(), source()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production source evidence must match confirmation sources");
  }

  private static @NonNull ProductionBatchConfirmationRequest request(
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence> evidence,
      @NonNull ProductionSourceReference source
  ) {
    return new ProductionBatchConfirmationRequest(
        WorkspaceId.newId(),
        BatchName.of("Dry cider"),
        VesselId.newId(),
        ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
            source,
            VolumeValue.ofLiters("100")
        ))),
        evidence,
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.055"),
            TitratableAcidityValue.of("6.5"),
            null
        ),
        null
    );
  }

  private static @NonNull ProductionSourceReference source() {
    return ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
  }

  private static @NonNull ProductionCandidateSourceEvidence evidence() {
    return new ProductionCandidateSourceEvidence(
        ProductionSourceEvidenceReference.of("measurement-evidence"),
        MaterialStateId.newId()
    );
  }
}
