package org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Translates Feedstock failures into the production batch source language. */
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
    } catch (FeedstockBatchNotFoundException exception) {
      throw new ProductionSourceNotFoundException(sourceReference);
    } catch (FeedstockBatchOwnershipMismatchException exception) {
      throw new ProductionSourceOwnershipMismatchException(
          sourceReference,
          exception.expectedWorkspaceId(),
          exception.actualWorkspaceId()
      );
    } catch (FeedstockBatchInsufficientAvailableVolumeException exception) {
      throw new ProductionSourceInsufficientAvailableVolumeException(
          sourceReference,
          exception.requestedVolume(),
          exception.availableVolume()
      );
    } catch (FeedstockBatchHasNoMeasurementsException
             | FeedstockMaterialCharacteristicsUnavailableException exception) {
      throw new ProductionSourceProfileUnavailableException(sourceReference);
    }
  }
}
