package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for correcting or completing an existing feedstock measurement.
 *
 * <p>The use case supports two semantics: replace the whole chemical profile or merge a patch into
 * the current profile. Merge mode is used when a cider maker gradually fills missing sugar,
 * acidity, tannin or pH values without losing previously entered axes.
 */
@Component
@RequiredArgsConstructor
public final class UpdateFeedstockMeasurementService
    implements UpdateFeedstockMeasurementUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockMeasurementSummary update(@NonNull UpdateFeedstockMeasurementCommand command) {
    requireNonNull(command, "update feedstock measurement command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
      final var measurement = feedstockMeasurementRepository.findById(command.measurementId())
          .orElseThrow(() -> new FeedstockMeasurementNotFoundException(command.measurementId()));

      if (!measurement.batchId().equals(command.batchId())) {
        throw new FeedstockMeasurementNotFoundException(command.measurementId());
      }

      if (command.name() != null) {
        measurement.rename(command.name());
      }
      if (command.chemicalProfile() != null) {
        measurement.changeChemicalProfile(command.mergeChemicalProfile()
            ? merge(measurement.chemicalProfile(), command.chemicalProfile())
            : command.chemicalProfile());
      }
      if (command.measuredAt() != null) {
        measurement.changeMeasuredAt(command.measuredAt());
      }
      if (command.clearNote()) {
        measurement.clearNote();
      } else if (command.note() != null) {
        measurement.changeNote(command.note());
      }

      final var saved = feedstockMeasurementRepository.save(measurement);

      return new FeedstockMeasurementSummary(
          saved.measurementId(),
          batch.ciderMakerId(),
          saved.batchId(),
          saved.name(),
          saved.measuredAt(),
          saved.chemicalProfile(),
          saved.note()
      );
    });
  }

  private static @NonNull ChemicalProfile merge(
      @NonNull ChemicalProfile current,
      @NonNull ChemicalProfile patch
  ) {
    final var builder = ChemicalProfile.builder();

    patch.sugar().or(current::sugar).ifPresent(builder::sugar);
    patch.ph().or(current::ph).ifPresent(builder::ph);
    patch.titratableAcidity().or(current::titratableAcidity).ifPresent(builder::titratableAcidity);
    patch.tanninity().or(current::tanninity).ifPresent(builder::tanninity);
    patch.yeastAssimilableNitrogen()
        .or(current::yeastAssimilableNitrogen)
        .ifPresent(builder::yeastAssimilableNitrogen);

    if (patch.source() != null) {
      builder.source(patch.source());
    } else if (current.source() != null) {
      builder.source(current.source());
    }

    builder.confidence(patch.confidence() == null ? current.confidence() : patch.confidence());

    return builder.build();
  }
}
