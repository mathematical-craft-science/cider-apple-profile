package org.craft.mcs.cap.infrastructure.adapter.out.production.source;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Translates Feedstock application failures into the Production source language. */
final class FeedstockProductionSourceFailureTranslator {

  private FeedstockProductionSourceFailureTranslator() {
  }

  static <T extends @NonNull Object> @NonNull T translate(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull Supplier<@NonNull T> operation
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
