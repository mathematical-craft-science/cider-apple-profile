package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchMaterialSnapshotChangedException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceInsufficientVolumeException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateStaleException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Translates Feedstock failures into the production planning source language. */
final class FeedstockProductionSourceFailureTranslator {

  private FeedstockProductionSourceFailureTranslator() {
  }

  static <T extends @NonNull Object> T translate(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull Supplier<T> operation
  ) {
    requireNonNull(sourceReference, "production source reference cannot be null");
    requireNonNull(operation, "feedstock source operation cannot be null");
    try {
      return operation.get();
    } catch (FeedstockBatchMaterialSnapshotChangedException exception) {
      throw new ProductionBatchCandidateStaleException(sourceReference);
    } catch (FeedstockBatchNotFoundException exception) {
      throw new ProductionPlanningSourceNotFoundException(sourceReference);
    } catch (FeedstockBatchOwnershipMismatchException exception) {
      throw new ProductionPlanningSourceOwnershipMismatchException(
          sourceReference,
          exception.expectedWorkspaceId(),
          exception.actualWorkspaceId()
      );
    } catch (FeedstockBatchInsufficientAvailableVolumeException exception) {
      throw new ProductionPlanningSourceInsufficientVolumeException(
          sourceReference,
          exception.requestedVolume(),
          exception.availableVolume()
      );
    } catch (FeedstockBatchHasNoMeasurementsException
             | FeedstockMaterialCharacteristicsUnavailableException exception) {
      throw new ProductionPlanningSourceProfileUnavailableException(sourceReference);
    }
  }
}
