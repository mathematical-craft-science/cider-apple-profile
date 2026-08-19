package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch;

import org.craft.mcs.cap.core.production.batch.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.batch.application.creation.ProductionSourceEvidenceConflictException;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceInsufficientVolumeException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceProfileUnavailableException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch.ProductionBatchConfirmationFailureTranslator.translate;

class ProductionBatchConfirmationFailureTranslatorTest {

  private static final ProductionSourceReference SOURCE_REFERENCE =
      ProductionSourceReference.feedstockBatch(java.util.UUID.fromString(
          "019c07d8-a720-7000-8000-000000000001"
      ));

  @Test
  void returnsSuccessfulConfirmationResult() {
    assertThat(translate(() -> "created"))
        .isEqualTo("created");
  }

  @Test
  void translatesChangedEvidenceToStaleCandidate() {
    assertThatThrownBy(() -> translate(() -> {
      throw new ProductionSourceEvidenceConflictException(SOURCE_REFERENCE);
    }))
        .isInstanceOf(ProductionBatchCandidateStaleException.class)
        .satisfies(exception -> assertThat((ProductionBatchCandidateStaleException) exception)
            .returns(SOURCE_REFERENCE, ProductionBatchCandidateStaleException::sourceReference));
  }

  @Test
  void translatesMissingSourceToPlanningLanguage() {
    assertThatThrownBy(() -> translate(() -> {
      throw new ProductionSourceNotFoundException(SOURCE_REFERENCE);
    }))
        .isInstanceOf(ProductionPlanningSourceNotFoundException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceNotFoundException) exception)
            .returns(SOURCE_REFERENCE, ProductionPlanningSourceNotFoundException::sourceReference));
  }

  @Test
  void translatesOwnershipMismatchWithoutLosingWorkspaceFacts() {
    final var expectedWorkspaceId = WorkspaceId.newId();
    final var actualWorkspaceId = WorkspaceId.newId();

    assertThatThrownBy(() -> translate(() -> {
      throw new ProductionSourceOwnershipMismatchException(
          SOURCE_REFERENCE,
          expectedWorkspaceId,
          actualWorkspaceId
      );
    }))
        .isInstanceOf(ProductionPlanningSourceOwnershipMismatchException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceOwnershipMismatchException) exception)
            .returns(SOURCE_REFERENCE, ProductionPlanningSourceOwnershipMismatchException::sourceReference)
            .returns(expectedWorkspaceId, ProductionPlanningSourceOwnershipMismatchException::expectedWorkspaceId)
            .returns(actualWorkspaceId, ProductionPlanningSourceOwnershipMismatchException::actualWorkspaceId));
  }

  @Test
  void translatesInsufficientVolumeWithoutLosingVolumeFacts() {
    final var requestedVolume = VolumeValue.ofLiters("100");
    final var availableVolume = VolumeValue.ofLiters("80");

    assertThatThrownBy(() -> translate(() -> {
      throw new ProductionSourceInsufficientAvailableVolumeException(
          SOURCE_REFERENCE,
          requestedVolume,
          availableVolume
      );
    }))
        .isInstanceOf(ProductionPlanningSourceInsufficientVolumeException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceInsufficientVolumeException) exception)
            .returns(SOURCE_REFERENCE, ProductionPlanningSourceInsufficientVolumeException::sourceReference)
            .returns(requestedVolume, ProductionPlanningSourceInsufficientVolumeException::requestedVolume)
            .returns(availableVolume, ProductionPlanningSourceInsufficientVolumeException::availableVolume));
  }

  @Test
  void translatesUnavailableProfileToPlanningLanguage() {
    assertThatThrownBy(() -> translate(() -> {
      throw new ProductionSourceProfileUnavailableException(SOURCE_REFERENCE);
    }))
        .isInstanceOf(ProductionPlanningSourceProfileUnavailableException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceProfileUnavailableException) exception)
            .returns(SOURCE_REFERENCE, ProductionPlanningSourceProfileUnavailableException::sourceReference));
  }
}
