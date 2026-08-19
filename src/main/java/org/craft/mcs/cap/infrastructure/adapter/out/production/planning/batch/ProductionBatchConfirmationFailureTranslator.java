package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.batch.application.creation.ProductionSourceEvidenceConflictException;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceInsufficientVolumeException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceProfileUnavailableException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Translates production-batch creation failures into the planning context language. */
final class ProductionBatchConfirmationFailureTranslator {

  private ProductionBatchConfirmationFailureTranslator() {
  }

  static <T extends @NonNull Object> T translate(
      @NonNull Supplier<T> action
  ) {
    requireNonNull(action, "production batch confirmation action cannot be null");
    try {
      return action.get();
    } catch (ProductionSourceEvidenceConflictException exception) {
      throw new ProductionBatchCandidateStaleException(exception.sourceReference());
    } catch (ProductionSourceNotFoundException exception) {
      throw new ProductionPlanningSourceNotFoundException(exception.sourceReference());
    } catch (ProductionSourceOwnershipMismatchException exception) {
      throw new ProductionPlanningSourceOwnershipMismatchException(
          exception.sourceReference(),
          exception.expectedWorkspaceId(),
          exception.actualWorkspaceId()
      );
    } catch (ProductionSourceInsufficientAvailableVolumeException exception) {
      throw new ProductionPlanningSourceInsufficientVolumeException(
          exception.sourceReference(),
          exception.requestedVolume(),
          exception.availableVolume()
      );
    } catch (ProductionSourceProfileUnavailableException exception) {
      throw new ProductionPlanningSourceProfileUnavailableException(exception.sourceReference());
    }
  }
}
