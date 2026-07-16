package org.craft.mcs.cap.core.feedstock.application.measurement.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.UpdateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
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
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull FeedstockMeasurementSummary update(@NonNull UpdateFeedstockMeasurementCommand command) {
    requireNonNull(command, "update feedstock measurement command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> updateInTransaction(command));
  }

  private @NonNull FeedstockMeasurementSummary updateInTransaction(
      @NonNull UpdateFeedstockMeasurementCommand command
  ) {
    final var batch = requireBatch(command);
    final var state = requireMeasurementFromBatch(command);
    final var measurement = state.aggregate();
    applyChanges(measurement, command);
    return toSummary(
        batch,
        feedstockMeasurementRepository.update(state.withAggregate(measurement)).aggregate()
    );
  }

  private @NonNull FeedstockBatch requireBatch(@NonNull UpdateFeedstockMeasurementCommand command) {
    final var batch = feedstockBatchRepository.findById(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    feedstockAccessControl.requireWriteAccess(batch.workspaceId());
    return batch;
  }

  private @NonNull VersionedAggregate<FeedstockMeasurement> requireMeasurementFromBatch(
      @NonNull UpdateFeedstockMeasurementCommand command
  ) {
    final var state = feedstockMeasurementRepository.findVersionedById(command.measurementId())
        .orElseThrow(() -> new FeedstockMeasurementNotFoundException(command.measurementId()));
    final var measurement = state.aggregate();
    if (!measurement.batchId().equals(command.batchId())) {
      throw new FeedstockMeasurementNotFoundException(command.measurementId());
    }
    return state;
  }

  private void applyChanges(
      @NonNull FeedstockMeasurement measurement,
      @NonNull UpdateFeedstockMeasurementCommand command
  ) {
    final var name = command.name();
    if (name != null) {
      measurement.rename(name);
    }
    applyChemicalProfileChange(measurement, command);
    final var measuredAt = command.measuredAt();
    if (measuredAt != null) {
      measurement.changeMeasuredAt(measuredAt, clock.instant());
    }
    applyNoteChange(measurement, command);
  }

  private static void applyChemicalProfileChange(
      @NonNull FeedstockMeasurement measurement,
      @NonNull UpdateFeedstockMeasurementCommand command
  ) {
    final var profile = command.chemicalProfile();
    if (profile == null) {
      return;
    }
    measurement.changeChemicalProfile(command.mergeChemicalProfile()
        ? merge(measurement.chemicalProfile(), profile)
        : profile);
  }

  private static void applyNoteChange(
      @NonNull FeedstockMeasurement measurement,
      @NonNull UpdateFeedstockMeasurementCommand command
  ) {
    if (command.clearNote()) {
      measurement.clearNote();
      return;
    }
    if (command.note() != null) {
      measurement.changeNote(command.note());
    }
  }

  private static @NonNull FeedstockMeasurementSummary toSummary(
      @NonNull FeedstockBatch batch,
      @NonNull FeedstockMeasurement measurement
  ) {
    return new FeedstockMeasurementSummary(
        measurement.measurementId(),
        batch.workspaceId(),
        measurement.batchId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
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
